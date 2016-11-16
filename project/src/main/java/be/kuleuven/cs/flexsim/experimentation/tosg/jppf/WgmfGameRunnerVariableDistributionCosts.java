package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WhoGetsMyFlexGame;
import be.kuleuven.cs.flexsim.experimentation.tosg.stat.EgtResultParser;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGame;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections15.map.UnmodifiableMap;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public static final String PRICE_PARAM_KEY = "DISTRIBUTION_E_S_PRICE";
    //    private final ConfigurableGameDirector director;

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
        //        this.director = new ConfigurableGameDirector(game);
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
            ConfigurableGameDirector director = new ConfigurableGameDirector(game);
            priceToDirector.put(price, director);

            List<GameInstanceConfiguration> priceContainingConfigs = director.getPlayableVersions()
                    .stream()
                    .map((config) -> config.withExtraConfigValue(PRICE_PARAM_KEY, price))
                    .collect(Collectors.toList());

            List<WgmfJppfTask> adapted = getStrategy()
                    .adapt(priceContainingConfigs, params, PARAMS_KEY,
                            (WgmfGameParams wgmfParams, GameInstanceConfiguration instanceConfig)
                                    -> WhoGetsMyFlexGame
                                    .createVariableDSOPricingGame(wgmfParams,
                                            instanceConfig.getSeed(),
                                            instanceConfig.getExtraConfigValues()
                                                    .get(PRICE_PARAM_KEY)));
            directorToTasks.putAll(director, adapted);
            alltasks.addAll(adapted);
        }

        ExperimentRunner runner = getStrategy().getRunner(params, PARAMS_KEY);
        runner.runExperiments(alltasks);
        List<?> results = runner.waitAndGetResults();
        logger.info("Experiment results received. \nProcessing results... ");
        getStrategy().processExecutionResults(results, PRICE_PARAM_KEY,
                UnmodifiableMap.decorate(priceToDirector));
        //        getStrategy().processExecutionResults(results, director);
    }

    @Override
    protected void processResults() {
        try (EgtResultParser egtResultParser = new EgtResultParser(null)) {
            for (double price : priceToDirector.keySet()) {
                ImmutableList<Double> eqnParams = priceToDirector.get(price).getResults()
                        .getResults();
                double[] fixedPoints = egtResultParser
                        .findFixedPointForDynEquationParams(
                                eqnParams.stream().mapToDouble(Double::doubleValue).toArray());
                logger.warn("Results for pricepoint {}:", price);
                logger.warn("Dynamics equation params: {}",
                        priceToDirector.get(price).getDynamicEquationArguments());
                logger.warn("Phase plot fixed points found at: {}", Arrays.toString(fixedPoints));
            }
        } catch (Exception e) {
            logger.error("Something went wrong parsing the results", e);
        }
    }
}