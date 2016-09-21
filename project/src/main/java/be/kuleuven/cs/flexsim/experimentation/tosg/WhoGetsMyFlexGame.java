package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.DistributionGridCongestionSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.WindErrorGenerator;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.poc.WindBasedInputData;
import be.kuleuven.cs.gametheory.GameInstance;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Game representing the strategic choice of providing your flexibility as a flex consumer to 1)
 * DSOs for grid congestion management, or 2) to portfolio balancers.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WhoGetsMyFlexGame implements GameInstance<FlexibilityProvider, FlexibilityUtiliser> {

    private final Set<FlexibilityProvider> agents;
    private final List<FlexibilityUtiliser> actions;
    private final Map<FlexibilityProvider, FlexibilityUtiliser> agentActionMap;
    private final CongestionProfile c1;
    private final CableCurrentProfile c2;
    private final TurbineSpecification specs;
    private final WindErrorGenerator generator;

    /**
     * Default Constructor
     *
     * @param dataIn         The data profile to work from.
     * @param specs          The specs of the windturbine used in these simulations.
     * @param gen            The generator instance for generating wind forecast errors.
     * @param solverplatform The specific solver factory platform to use.
     */
    public WhoGetsMyFlexGame(WindBasedInputData dataIn, TurbineSpecification specs,
            WindErrorGenerator gen, AbstractSolverFactory<SolutionResults> solverplatform) {
        this.generator = gen;
        this.agents = Sets.newLinkedHashSet();
        this.actions = Lists.newArrayList();
        //            this.nAgents = nAgents;
        this.agentActionMap = Maps.newLinkedHashMap();
        this.c1 = dataIn.getCongestionProfile();
        this.c2 = dataIn.getCableCurrentProfile();
        this.specs = specs;
        this.actions.add(new PortfolioBalanceSolver(solverplatform, this.c2, specs, generator));
        this.actions.add(new DistributionGridCongestionSolver(solverplatform, this.c1));
    }

    @Override
    public Map<FlexibilityProvider, Long> getPayOffs() {
        Map<FlexibilityProvider, Long> results = Maps.newLinkedHashMap();
        agentActionMap.forEach(
                (agent, action) -> results.put(agent, agent.getMonetaryCompensationValue()));
        return results;
    }

    @Override
    public void fixActionToAgent(FlexibilityProvider agent, FlexibilityUtiliser action) {
        agents.add(agent);
        agentActionMap.put(agent, action);
    }

    @Override
    public void init() {
        agentActionMap.forEach((FlexibilityProvider fp, FlexibilityUtiliser action) -> action
                .registerFlexProvider(fp));

    }

    @Override
    public List<FlexibilityUtiliser> getActionSet() {
        return Collections.unmodifiableList(actions);
    }

    @Override
    public Map<FlexibilityProvider, FlexibilityUtiliser> getAgentToActionMapping() {
        return Collections.unmodifiableMap(agentActionMap);
    }

    @Override
    public long getExternalityValue() {
        return 0;
    }

    @Override
    public void play() {
        new SimulatedGamePlayAdapter(actions).play();
    }

}
