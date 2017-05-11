package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gametheory.configurable.AbstractGameInstance;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.DistributionGridCongestionSolver;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.SolverInputData;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.experimentation.tosg.adapters.SimulatedGamePlayAdapter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Game representing the strategic choice of providing your flexibility as a flex consumer to 1)
 * DSOs for grid congestion management, or 2) to portfolio balancers.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WhoGetsMyFlexGame extends
                               AbstractGameInstance<FlexibilityProvider, FlexibilityUtiliser> {

    private static final int TO_LONG_SCALE = 100;

    /**
     * Constructor callable from extensions.
     *
     * @param providerList The list of providers.
     */
    protected WhoGetsMyFlexGame(List<FlexibilityUtiliser> providerList) {
        super(providerList);
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
    public double getExternalityValue() {
        return getActionSet().stream()
                .mapToDouble(a -> a.getSolution().getNormalizedObjectiveValue()).sum();
    }

    @Override
    public void play() {
        new SimulatedGamePlayAdapter(getActionSet()).play();
    }

    /**
     * Factory method for creating a basic game.
     *
     * @param params   The input params.
     * @param baseSeed The seed to use.
     * @return A fully built game object.
     */
    public static WhoGetsMyFlexGame createBasicGame(WgmfGameParams params, long baseSeed) {
        AbstractSolverFactory<SolutionResults> solverplatform = params.getFactory();
        SolverInputData solverInputData = params.toSolverInputData(baseSeed);

        ArrayList<FlexibilityUtiliser> actions = Lists
                .newArrayList(new PortfolioBalanceSolver(solverplatform, solverInputData),
                        new DistributionGridCongestionSolver(solverplatform,
                                solverInputData.getCongestionProfile()));
        return new WhoGetsMyFlexGame(actions);
    }

    /**
     * Factory method for creating a game with variable pricing for the dso.
     *
     * @param params                The input params.
     * @param baseSeed              The seed to use.
     * @param flexRemunerationPrice The pricing rate for the DSO.
     * @return A fully built game object.
     */
    public static WhoGetsMyFlexGame createVariableDSOPricingGame(WgmfGameParams params,
            long baseSeed, double flexRemunerationPrice) {
        SolverInputData solverInputData = params.toSolverInputData(baseSeed);

        WgmfSolverFactory solverplatform = params.getFactory();
        //        solverplatform.setSeed(baseSeed);//TODO review! This makes the solver also random.
        ArrayList<FlexibilityUtiliser> actions = Lists
                .newArrayList(new PortfolioBalanceSolver(solverplatform, solverInputData),
                        new DistributionGridCongestionSolver(solverplatform,
                                solverInputData.getCongestionProfile(), flexRemunerationPrice));
        return new WhoGetsMyFlexGame(actions);
    }
}
