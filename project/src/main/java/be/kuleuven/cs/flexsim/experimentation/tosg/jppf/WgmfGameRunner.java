package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.stat.EgtResultParser;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGame;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import com.google.common.collect.ImmutableList;
import org.jppf.node.protocol.Task;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.LOCAL;
import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.REMOTE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Game runner for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunner extends AbstractWgmfGameRunner {
    private static final Logger logger = getLogger(WgmfGameRunner.class);
    private final ConfigurableGameDirector director;

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP  The experiment parameters.
     * @param strat The execution strategy.
     */
    public WgmfGameRunner(ExperimentParams expP, ExecutionStrategy strat) {
        super(strat);
        ConfigurableGame game = new ConfigurableGame(expP.getNAgents(),
                ACTION_SIZE, expP.getNRepititions());
        this.director = new ConfigurableGameDirector(game);
    }

    private WgmfGameRunner(ExperimentParams expP) {
        this(expP, expP.runRemote() ? REMOTE : LOCAL);
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        startExecution(args, (params) -> new WgmfGameRunner(params));
    }

    @Override
    protected void execute(WgmfGameParams params) {
        List<WgmfJppfTask> adapted = getStrategy().adapt(director, params, PARAMS_KEY);
        ExperimentRunner runner = getStrategy().getRunner(params, PARAMS_KEY);
        runner.runExperiments(adapted);
        List<Task<?>> results = runner.waitAndGetResults();
        logger.info("Experiment results received. \nProcessing results... ");
        getStrategy().processExecutionResults(results, director);
    }

    @Override
    protected void logResults() {
        try (EgtResultParser egtResultParser = new EgtResultParser(null)) {
            ImmutableList<Double> eqnParams = director.getResults().getResults();
            double[] fixedPoints = egtResultParser
                    .findFixedPointForDynEquationParams(
                            eqnParams.stream().mapToDouble(Double::doubleValue).toArray());
            logger.warn("Phase plot fixed points found at: {}", Arrays.toString(fixedPoints));
        } catch (Exception e) {
            logger.error("Something went wrong parsing the results", e);
        }
        logger.warn("Dynamics equation params: {}", director.getDynamicEquationArguments());
        logger.warn("Payoff table: \n{}",
                director.getFormattedResults().getFormattedResultString());
    }
}