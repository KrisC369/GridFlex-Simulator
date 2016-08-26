package be.kuleuven.cs.flexsim.experimentation.tosg.poc;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.DistributionGridCongestionSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import be.kuleuven.cs.flexsim.solver.optimal.dso.DSOOptimalSolver;
import be.kuleuven.cs.gametheory.Game;
import be.kuleuven.cs.gametheory.GameConfigurator;
import be.kuleuven.cs.gametheory.GameDirector;
import be.kuleuven.cs.gametheory.GameInstance;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PoCRunner {

    public static void main(String[] args) {
        PoCConfigurator poCConfigurator = new PoCConfigurator();
        Game<FlexibilityProvider, FlexibilityUtiliser> game = new Game<>(2, poCConfigurator, 2);
        GameDirector director = new GameDirector(game);
        director.playAutonomously();
        System.out.println(director.getFormattedResults().getFormattedResultString());
    }

    private static final String FILE = "2kwartOpEnNeer.csv";
    private static final String COLUMN = "verlies aan energie";
    private static final int NAGENTS = 2;

    public PoCRunner() {
    }

    private void pocInstantiation() {
        CongestionProfile c = CongestionProfile.empty();
        Simulator s;
        try {
            c = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        s = Simulator.createSimulator(1000);
        AbstractSolverFactory<SolutionResults> fact = new AbstractSolverFactory<SolutionResults>
                () {
            @Override
            public Solver<SolutionResults> createSolver(FlexAllocProblemContext context) {
                return new SolverAdapter<AllocResults, SolutionResults>(
                        new DSOOptimalSolver(context, AbstractOptimalSolver.Solver.CPLEX)) {

                    @Override
                    public SolutionResults adaptResult(AllocResults solution) {
                        return SolutionResults.EMPTY;
                    }
                };
            }
        };
        FlexProvider p1 = new FlexProvider(300);
        FlexProvider p2 = new FlexProvider(300);
        PortfolioBalanceSolver tso = new PortfolioBalanceSolver(fact, c);
        DistributionGridCongestionSolver dso = new DistributionGridCongestionSolver(fact, c);
        tso.registerFlexProvider(p1);
        dso.registerFlexProvider(p2);
        dso.solve();
        tso.solve();
        SolutionResults r1 = dso.getSolution();
        SolutionResults r2 = tso.getSolution();
        System.out.println(r1);
        System.out.println(r2);
    }

    public static class PoCConfigurator
            implements GameConfigurator<FlexibilityProvider, FlexibilityUtiliser> {
        private static final double R3DP_GAMMA_SCALE = 677.926;
        private static final double R3DP_GAMMA_SHAPE = 1.37012;
        private static final long SEED = 1312421L;
        private CongestionProfile c;
        final GammaDistribution gd;

        public PoCConfigurator() {
            gd = new GammaDistribution(new MersenneTwister(SEED),
                    R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
            try {
                c = (CongestionProfile) CongestionProfile
                        .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");

            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public FlexibilityProvider getAgent() {
            return new FlexProvider(gd.sample());
        }

        @Override
        public GameInstance<FlexibilityProvider, FlexibilityUtiliser> generateInstance() {
            return new PoCGame(c);
        }

        @Override
        public int getActionSpaceSize() {
            return 2;
        }
    }

    public static class PoCGame implements
                                GameInstance<FlexibilityProvider, FlexibilityUtiliser> {

        //        private final int nAgents;
        private final Set<FlexibilityProvider> agents;
        private final List<FlexibilityUtiliser> actions;

        private final Map<FlexibilityProvider, FlexibilityUtiliser> agentActionMap;
        private CongestionProfile c;

        public PoCGame(CongestionProfile c) {
            agents = Sets.newLinkedHashSet();
            actions = Lists.newArrayList();
            //            this.nAgents = nAgents;
            agentActionMap = Maps.newLinkedHashMap();
            this.c = c;
            AbstractSolverFactory<SolutionResults> fact = new AbstractSolverFactory<SolutionResults>
                    () {
                @Override
                public Solver<SolutionResults> createSolver(FlexAllocProblemContext context) {
                    return new SolverAdapter<AllocResults, SolutionResults>(
                            new DSOOptimalSolver(context, AbstractOptimalSolver.Solver.CPLEX)) {

                        @Override
                        public SolutionResults adaptResult(AllocResults solution) {
                            return SolutionResults.EMPTY;
                            //TODO implement
                        }
                    };
                }
            };
            actions.add(new PortfolioBalanceSolver(fact, c));
            actions.add(new DistributionGridCongestionSolver(fact, c));
        }

        @Override
        public Map<FlexibilityProvider, Long> getPayOffs() {
            Map<FlexibilityProvider, Long> results = Maps.newLinkedHashMap();
            agentActionMap.forEach(
                    (agent, action) -> results.put(agent, agent.getMonetaryCompensationValue()));
            return results;
        }

        @Override
        public void fixActionToAgent(FlexibilityProvider agent,
                FlexibilityUtiliser action) {
            agents.add(agent);
            agentActionMap.put(agent, action);
        }

        @Override
        public void init() {
            agentActionMap.forEach(
                    (FlexibilityProvider fp,
                            FlexibilityUtiliser action) -> action.registerFlexProvider(fp));

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
}
