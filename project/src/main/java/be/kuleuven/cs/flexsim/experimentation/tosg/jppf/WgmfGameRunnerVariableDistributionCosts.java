package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WhoGetsMyFlexGame;
import be.kuleuven.cs.flexsim.experimentation.tosg.stat.EgtResultParser;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGame;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jppf.node.protocol.Task;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.LOCAL;
import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.REMOTE;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Game runner for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunnerVariableDistributionCosts extends AbstractWgmfGameRunner {
    private static final Logger logger = getLogger(WgmfGameRunnerVariableDistributionCosts.class);
    private final ConfigurableGameDirector director;

    private int nAgents, nReps;
    private final Map<Double, ConfigurableGameDirector> priceToDirector;
    private final LinkedListMultimap<ConfigurableGameDirector, WgmfJppfTask> directorToTasks;

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP  The experiment parameters.
     * @param strat The execution strategy.
     */
    public WgmfGameRunnerVariableDistributionCosts(ExperimentParams expP, ExecutionStrategy strat) {
        super(strat);
        this.nAgents = expP.getNAgents();
        this.nReps = expP.getNRepititions();
        ConfigurableGame game = new ConfigurableGame(expP.getNAgents(),
                ACTION_SIZE, expP.getNRepititions());
        this.director = new ConfigurableGameDirector(game);
        priceToDirector = Maps.newLinkedHashMap();
        directorToTasks = LinkedListMultimap.create();
    }

    private WgmfGameRunnerVariableDistributionCosts(ExperimentParams expP) {
        this(expP, expP.runRemote() ? REMOTE : LOCAL);
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        startExecution(args, (params) -> new WgmfGameRunnerVariableDistributionCosts(params));
    }

    @Override
    protected void execute(WgmfGameParams params) {
        double minPrice = 35.4;
        double maxPrice = 61.1;
        double priceStep = 10;
        List<WgmfJppfTask> alltasks = Lists.newArrayList();
        for (double p = minPrice; p <= maxPrice; p += priceStep) {
            final double price = p;
            ConfigurableGame game = new ConfigurableGame(nAgents,
                    ACTION_SIZE, nReps);
            List<WgmfJppfTask> adapted = getStrategy().adapt(director, params, PARAMS_KEY,
                    (WgmfGameParams wgmfParams, long seed) -> WhoGetsMyFlexGame
                            .createVariableDSOPricingGame(wgmfParams, seed, price));
            ConfigurableGameDirector director = new ConfigurableGameDirector(game);
            priceToDirector.put(price, director);
            directorToTasks.putAll(director, adapted);
            alltasks.addAll(adapted);
        }

        ExperimentRunner runner = getStrategy().getRunner(params, PARAMS_KEY);
        runner.runExperiments(alltasks);
        List<Task<?>> results = runner.waitAndGetResults();
        logger.info("Experiment results received. \nProcessing results... ");
        //        getStrategy().processExecutionResults(results, director);
        for (double price : priceToDirector.keySet()) {
            getStrategy().processExecutionResults(directorToTasks.get(priceToDirector.get(price)),
                    priceToDirector.get(price));
        }
    }

    @Override
    protected void logResults() {
        try (EgtResultParser egtResultParser = new EgtResultParser(null)) {
            for (double price : priceToDirector.keySet()) {
                ImmutableList<Double> eqnParams = priceToDirector.get(price).getResults()
                        .getResults();
                double[] fixedPoints = egtResultParser
                        .findFixedPointForDynEquationParams(
                                eqnParams.stream().mapToDouble(Double::doubleValue).toArray());
                logger.warn("Results for pricepoint {}:", price);
                logger.warn("Phase plot fixed points found at: {}", Arrays.toString(fixedPoints));
            }
        } catch (Exception e) {
            logger.error("Something went wrong parsing the results", e);
        }
        logger.warn("Dynamics equation params: {}", director.getDynamicEquationArguments());
        logger.warn("Payoff table: \n{}",
                director.getFormattedResults().getFormattedResultString());
    }
}