package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WhoGetsMyFlexGame;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.CsvResultWriter;
import be.kuleuven.cs.flexsim.experimentation.tosg.stat.EgtResultParser;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGame;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.evolutionary.EvolutionaryGameDynamics;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections15.map.UnmodifiableMap;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.LOCAL;
import static be.kuleuven.cs.flexsim.experimentation.tosg.jppf.ExecutionStrategy.REMOTE;
import static com.google.common.base.Preconditions.checkArgument;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Game runner for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfMultiJobGameRunnerVariableDistributionCosts extends AbstractWgmfGameRunner {
    private static final Logger logger = getLogger(
            WgmfMultiJobGameRunnerVariableDistributionCosts.class);
    public static final String PRICE_PARAM_KEY = "DISTRIBUTION_E_S_PRICE";
    public static final EvolutionaryGameDynamics.ConfidenceLevel CI_LEVEL = EvolutionaryGameDynamics
            .ConfidenceLevel._95pc;
    private static final String RES_OUTPUT_FILE = "res/res_outputA";

    private int nAgents, nReps;
    private final LinkedListMultimap<ConfigurableGameDirector, WgmfJppfTask> directorToTasks;
    private double minPrice;
    private double maxPrice;
    private double priceStep;

    private final List<CsvResultWriter.WgmfDynamicsResults> writableResults;

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP  The experiment parameters.
     * @param strat The execution strategy.
     */
    public WgmfMultiJobGameRunnerVariableDistributionCosts(ExperimentParams expP) {
        super(expP.isRemoteExecutable() ? REMOTE : LOCAL);
        this.nAgents = expP.getNAgents();
        this.nReps = expP.getNRepititions();
        ConfigurableGame game = new ConfigurableGame(expP.getNAgents(),
                ACTION_SIZE, expP.getNRepititions());
        directorToTasks = LinkedListMultimap.create();
        this.minPrice = expP.getP1Start();
        this.priceStep = expP.getP1Step();
        this.maxPrice = expP.getP1End();
        writableResults = Lists.newArrayList();
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        startExecution(args,
                (params) -> new WgmfMultiJobGameRunnerVariableDistributionCosts(params));
    }

    @Override
    protected void execute(WgmfGameParams params) {
        for (double price = minPrice; price <= maxPrice; price += priceStep) {
            List<WgmfJppfTask> alltasks = Lists.newArrayList();

            ConfigurableGame game = new ConfigurableGame(nAgents,
                    ACTION_SIZE, nReps);
            ConfigurableGameDirector director = new ConfigurableGameDirector(game);

            Map<Double, ConfigurableGameDirector> priceToDirector = Maps.newLinkedHashMap();
            priceToDirector.put(price, director);

            List<GameInstanceConfiguration> priceContainingConfigs =
                    getConfigsWithPricesFromDirector(price, director);

            List<WgmfJppfTask> adapted = adaptPriceConfigsToRunnableTasks(params,
                    priceContainingConfigs);
            directorToTasks.putAll(director, adapted);
            alltasks.addAll(adapted);
            ExperimentRunner runner = getStrategy()
                    .getRunner(params, PARAMS_KEY, "JobWPrice:" + String.valueOf(price));
            runner.runExperiments(alltasks);
            List<?> results = runner.waitAndGetResults();
            logger.info("Experiment results received for price: {}. \nProcessing results... ",
                    price);
            getStrategy().processExecutionResults(results, PRICE_PARAM_KEY,
                    UnmodifiableMap.decorate(priceToDirector));
            processSingleResult(price, director);
        }

    }

    private List<WgmfJppfTask> adaptPriceConfigsToRunnableTasks(WgmfGameParams params,
            List<GameInstanceConfiguration> priceContainingConfigs) {
        return getStrategy().adapt(priceContainingConfigs, params, PARAMS_KEY,
                (WgmfGameParams wgmfParams, GameInstanceConfiguration instanceConfig) ->
                        WhoGetsMyFlexGame
                                .createVariableDSOPricingGame(wgmfParams, instanceConfig.getSeed(),
                                        instanceConfig.getExtraConfigValues()
                                                .get(PRICE_PARAM_KEY)));
    }

    private List<GameInstanceConfiguration> getConfigsWithPricesFromDirector(double price,
            ConfigurableGameDirector director) {
        return director.getPlayableVersions().stream()
                .map((config) -> config.withExtraConfigValue(PRICE_PARAM_KEY, price))
                .collect(Collectors.toList());
    }

    protected void processSingleResult(Double price, ConfigurableGameDirector d) {
        List<CsvResultWriter.WgmfDynamicsResults> toWrite = Lists.newArrayList();
        try (EgtResultParser egtResultParser = new EgtResultParser(null)) {
            EvolutionaryGameDynamics dynamics = EvolutionaryGameDynamics
                    .from(d.getResults().getResults());
            double[] eqnParams = dynamics.getDynamicEquationFactors().stream()
                    .mapToDouble(Double::doubleValue).toArray();
            double[] lowerCI = getLowerCIParams(dynamics).stream()
                    .mapToDouble(Double::doubleValue).toArray();
            double[] higherCI = getHigherCIParams(dynamics).stream()
                    .mapToDouble(Double::doubleValue).toArray();
            double[] fixedPoints = egtResultParser
                    .findFixedPointForDynEquationParams(eqnParams);
            double[] fixedPointsLower = egtResultParser
                    .findFixedPointForDynEquationParams(lowerCI);
            double[] fixedPointsHigher = egtResultParser
                    .findFixedPointForDynEquationParams(higherCI);
            logger.warn("Results for pricepoint {}:", price);
            logger.warn("Dynamics equation params: {}",
                    d.getDynamicEquationArguments());
            logger.warn("Phase plot fixed points found at: {}", Arrays.toString(fixedPoints));
            logger.warn("{} CI Lower bound Phase plot fixed points found at: {}",
                    CI_LEVEL.getConfidenceLevel(), Arrays.toString(fixedPointsLower));
            logger.warn("{} CI Upper bound Phase plot fixed points found at: {}",
                    CI_LEVEL.getConfidenceLevel(), Arrays.toString(fixedPointsHigher));
            String[] splitted = DATAFILE.split("/");
            writableResults.add(CsvResultWriter.WgmfDynamicsResults
                    .create(nAgents, nReps, splitted[splitted.length - 1], price,
                            fixedPoints,
                            fixedPointsLower,
                            fixedPointsHigher, eqnParams, lowerCI, higherCI,
                            CI_LEVEL.getConfidenceLevel()));
        } catch (Exception e) {
            logger.error("Something went wrong parsing the results", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void processResults() {
        CsvResultWriter.writeCsvFile(
                RES_OUTPUT_FILE + String.valueOf(nAgents) + "R" + String.valueOf(nReps) + "_"
                        + String
                        .valueOf(System.currentTimeMillis() / 100), writableResults);
    }

    private List<Double> getHigherCIParams(EvolutionaryGameDynamics dynamics) {
        return getGenericCIParams(dynamics, ci -> ci.getUpperBound(), ci -> ci.getLowerBound());
    }

    private List<Double> getLowerCIParams(EvolutionaryGameDynamics dynamics) {
        return getGenericCIParams(dynamics, ci -> ci.getLowerBound(), ci -> ci.getUpperBound());
    }

    private List<Double> getGenericCIParams(EvolutionaryGameDynamics dynamics,
            ToDoubleFunction<ConfidenceInterval> first, ToDoubleFunction<ConfidenceInterval> last) {
        List<ConfidenceInterval> cis = dynamics.getConfidenceIntervals(CI_LEVEL);
        checkArgument(!cis.isEmpty(), "Dynamics should not be empty.");
        List<Double> toRet = Lists.newArrayList();
        toRet.add(first.applyAsDouble(cis.get(0)));
        for (int i = 1; i < cis.size() - 1; i++) {
            if (i % 2 != 0) {
                toRet.add(first.applyAsDouble(cis.get(i)));
            } else {
                toRet.add(last.applyAsDouble(cis.get(i)));
            }
        }
        toRet.add(last.applyAsDouble(cis.get(cis.size() - 1)));
        return toRet;
    }
}