package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gametheory.configurable.ConfigurableGame;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.configurable.GameInstanceResult;
import be.kuleuven.cs.gametheory.evolutionary.EvolutionaryGameDynamics;
import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.gridflex.experimentation.tosg.stat.EgtResultParser;
import org.jppf.node.protocol.Task;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        this(expP, expP.isRemoteExecutable() ? ExecutionStrategy.REMOTE : ExecutionStrategy.LOCAL);
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        startExecution(args, WgmfGameRunner::new);
    }

    @Override
    protected void execute(WgmfGameParams params) {
        List<GameInstanceConfiguration> playableVersions = director.getPlayableVersions();
        List<GenericTask<GameInstanceResult>> tasks = playableVersions.stream()
                .map(conf -> new WgmfJppfTask(conf, params,
                        (WgmfGameParams wgmfParams, GameInstanceConfiguration config) ->
                                WhoGetsMyFlexGame
                                        .createBasicGame(wgmfParams, config.getSeed())))
                .collect(Collectors.toList());
        List<GenericTask<GameInstanceResult>> adapted = getStrategy()
                .adapt(tasks, PARAMS_KEY);

        ExperimentRunner runner = getStrategy().getRunner(params, PARAMS_KEY);
        runner.runExperiments(adapted);
        List<Task<?>> results = runner.waitAndGetResults();
        logger.info("Experiment results received. \nProcessing results... ");
        getStrategy().processExecutionResultsFailFast(results,
                (args) -> director.notifyVersionHasBeenPlayed((GameInstanceResult) args));
    }

    @Override
    protected void processResults() {
        try (EgtResultParser egtResultParser = new EgtResultParser(null)) {
            List<Double> eqnParams = EvolutionaryGameDynamics
                    .from(director.getResults().getResults()).getDynamicEquationFactors();
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