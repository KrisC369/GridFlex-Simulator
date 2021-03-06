package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;

import java.math.BigDecimal;

/**
 * A runnable task for executing Opta simulations.
 * Represents one single game instance.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class OptaJppfTaskPB extends GenericTask<OptaExperimentResults> {
    private static final long serialVersionUID = 5436262172692915491L;
    private WgmfGameParams params;
    private final long seed;
    private final int agents;
    private final HourlyFlexConstraints constraints;

    /**
     * Default constructor.
     *
     * @param instanceConfig The instance config value for this task.
     * @param s              The key for which to query the data provider for the instance
     *                       parameter data.
     */
    OptaJppfTaskPB(WgmfGameParams params, long seed, int agents, HourlyFlexConstraints constraints) {
        this.seed = seed;
        this.agents = agents;
        this.constraints = constraints;
        this.params = params;
    }

    @Override
    public void run() {
        PortfolioBalanceSolver portfolioBalanceSolver = new PortfolioBalanceSolver(
                params.getFactory(),
                params.toSolverInputData(seed),
                PortfolioBalanceSolver.ProfileConversionStrategy.POWER_ERROR_BASED);
        //generate agents
        WgmfAgentGenerator configurator = new WgmfAgentGenerator(seed,
                constraints);
        for (int i = 0; i < agents; i++) {
            portfolioBalanceSolver.registerFlexProvider(configurator.getAgent());
        }

        portfolioBalanceSolver.solve();
        SolutionResults solutionCPL = portfolioBalanceSolver.getSolution();
        setResult(OptaExperimentResults
                .create(BigDecimal.valueOf(solutionCPL.getObjectiveValue()),
                        solutionCPL.getNormalizedObjectiveValue(), constraints));
    }

    @Override
    public OptaExperimentResults call() throws Exception {
        run();
        return getResult();
    }
}
