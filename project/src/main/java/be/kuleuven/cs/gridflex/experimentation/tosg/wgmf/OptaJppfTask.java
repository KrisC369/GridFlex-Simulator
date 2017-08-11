package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import com.google.common.annotations.VisibleForTesting;
import org.jppf.node.protocol.AbstractTask;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

/**
 * A runnable task for executing Opta simulations.
 * Represents one single game instance.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class OptaJppfTask extends AbstractTask<OptaExperimentResults> implements Callable<Object> {
    //TODO generate serial version id when done.
    private WgmfGameParams params;
    private final String paramsDataKey;
    private final long seed;
    private final int agents;
    private final HourlyFlexConstraints constraints;
    //    private final GameInstanceFactory instanceFactory;

    /**
     * Default constructor.
     *
     * @param instanceConfig The instance config value for this task.
     * @param s              The key for which to query the data provider for the instance
     *                       parameter data.
     */
    public OptaJppfTask(String s, long seed, int agents, HourlyFlexConstraints constraints) {
        paramsDataKey = s;
        this.seed = seed;
        this.agents = agents;
        this.constraints = constraints;
    }

    @VisibleForTesting
    OptaJppfTask(WgmfGameParams params, long seed, int agents, HourlyFlexConstraints constraints) {
        this.seed = seed;
        this.agents = agents;
        this.constraints = constraints;
        this.paramsDataKey = "";
        this.params = params;
    }

    @Override
    public void run() {
        if (getDataProvider() != null) {
            params = getDataProvider().getParameter(paramsDataKey);
        }

        PortfolioBalanceSolver portfolioBalanceSolver = new PortfolioBalanceSolver(
                params.getFactory(),
                params.toSolverInputData(seed));
        //generate agents
        WgmfAgentGenerator configurator = new WgmfAgentGenerator(seed,
                constraints);
        for (int i = 0; i < agents; i++) {
            portfolioBalanceSolver.registerFlexProvider(configurator.getAgent());
        }

        portfolioBalanceSolver.solve();
        SolutionResults solutionCPL = portfolioBalanceSolver.getSolution();
        setResult(OptaExperimentResults
                .create(BigDecimal.valueOf(solutionCPL.getObjectiveValue()), constraints));
    }

    @Override
    public OptaExperimentResults call() throws Exception {
        run();
        return getResult();
    }

}
