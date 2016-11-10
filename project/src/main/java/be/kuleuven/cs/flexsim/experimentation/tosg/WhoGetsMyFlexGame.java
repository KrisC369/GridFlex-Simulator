package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.DistributionGridCongestionSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.WindErrorGenerator;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.adapters.SimulatedGamePlayAdapter;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.WindBasedInputData;
import be.kuleuven.cs.gametheory.configurable.AbstractGameInstance;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Game representing the strategic choice of providing your flexibility as a flex consumer to 1)
 * DSOs for grid congestion management, or 2) to portfolio balancers.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WhoGetsMyFlexGame extends
                               AbstractGameInstance<FlexibilityProvider, FlexibilityUtiliser> {

    public static final int TO_LONG_SCALE = 100;

    /**
     * Default Constructor
     *
     * @param dataIn         The data profile to work from.
     * @param specs          The specs of the windturbine used in these simulations.
     * @param imbalIn        The input imbalance price data.
     * @param gen            The generator instance for generating wind forecast errors.
     * @param solverplatform The specific solver factory platform to use.
     */
    private WhoGetsMyFlexGame(WindBasedInputData dataIn, TurbineSpecification specs,
            ImbalancePriceInputData imbalIn, DayAheadPriceProfile dap, WindErrorGenerator gen,
            AbstractSolverFactory<SolutionResults> solverplatform) {
        super(Lists.newArrayList(new PortfolioBalanceSolver(solverplatform,
                        dataIn.getCableCurrentProfile(), imbalIn
                        .getNetRegulatedVolumeProfile(),
                        imbalIn.getPositiveImbalancePriceProfile(), specs, gen, dap),
                new DistributionGridCongestionSolver(solverplatform,
                        dataIn.getCongestionProfile())));
    }

    /**
     * Constructor from param object.
     *
     * @param params   The input params for this game.
     * @param baseSeed The base seed to work from.
     */
    public WhoGetsMyFlexGame(WgmfGameParams params, long baseSeed) {
        this(params.getInputData(), params.getSpecs(), params.getImbalancePriceData(),
                params.getDayAheadPriceData(),
                new WindErrorGenerator(baseSeed, params.getDistribution()), params.getFactory());
    }

    @Override
    public Map<FlexibilityProvider, Double> getPayOffs() {
        Map<FlexibilityProvider, Double> results = Maps.newLinkedHashMap();
        getAgentToActionMapping().forEach(
                (agent, action) -> results.put(agent,
                        agent.getMonetaryCompensationValue() * TO_LONG_SCALE));
        return results;
    }

    @Override
    public void init() {
        getAgentToActionMapping()
                .forEach((FlexibilityProvider fp, FlexibilityUtiliser action) -> action
                        .registerFlexProvider(fp));
    }

    @Override
    public long getExternalityValue() {
        return 0;
    }

    @Override
    public void play() {
        new SimulatedGamePlayAdapter(getActionSet()).play();
    }

}
