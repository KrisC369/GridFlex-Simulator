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
import be.kuleuven.cs.gametheory.stats.ConfidenceLevel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.map.UnmodifiableMap;
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
public class WgmfGameRunnerVariableDistributionCosts extends AbstractWgmfGameRunner {
    public static final String PRICE_PARAM_KEY = "DISTRIBUTION_E_S_PRICE";
    public static final ConfidenceLevel CI_LEVEL = ConfidenceLevel._95pc;
    protected static final String RES_OUTPUT_FILE = "res/res_outputA";
    protected static final String RES_EXTENSION = ".csv";
    private static final Logger logger = getLogger(WgmfGameRunnerVariableDistributionCosts.class);
    private final Map<Double, ConfigurableGameDirector> priceToDirector;
    private final int nAgents;
    private final int nReps;
    private final double minPrice;
    private final double maxPrice;
    private final double priceStep;
    private final int windErrorFileIdx;
    private final int dataProfileIdx;

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP The experiment parameters.
     */
    public WgmfGameRunnerVariableDistributionCosts(ExperimentParams expP) {
        super(expP.isRemoteExecutable() ? REMOTE : LOCAL);
        this.nAgents = expP.getNAgents();
        this.nReps = expP.getNRepititions();
        priceToDirector = Maps.newLinkedHashMap();
        this.minPrice = expP.getP1Start();
        this.priceStep = expP.getP1Step();
        this.maxPrice = expP.getP1End();
        this.windErrorFileIdx = expP.getWindErrorProfileIndex();
        this.dataProfileIdx = expP.getCurrentDataProfileIndex();
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        startExecution(args, WgmfGameRunnerVariableDistributionCosts::new);
    }

    @Override
    protected void execute(WgmfGameParams params) {
        List<WgmfJppfTask> alltasks = Lists.newArrayList();
        for (double price = getMinPrice(); price <= getMaxPrice(); price += getPriceStep()) {
            ConfigurableGame game = new ConfigurableGame(getnAgents(),
                    ACTION_SIZE, getnReps());
            ConfigurableGameDirector director = new ConfigurableGameDirector(game);
            priceToDirector.put(price, director);

            List<GameInstanceConfiguration> priceContainingConfigs =
                    getConfigsWithPricesFromDirector(price, director);

            List<WgmfJppfTask> adapted = adaptPriceConfigsToRunnableTasks(params,
                    priceContainingConfigs);
            alltasks.addAll(adapted);
        }

        ExperimentRunner runner = getStrategy().getRunner(params, PARAMS_KEY);
        runner.runExperiments(alltasks);
        List<?> results = runner.waitAndGetResults();
        logger.info("Experiment results received. \nProcessing results... ");
        getStrategy().processExecutionResults(results, PRICE_PARAM_KEY,
                UnmodifiableMap.unmodifiableMap(priceToDirector));
    }

    protected final List<WgmfJppfTask> adaptPriceConfigsToRunnableTasks(WgmfGameParams params,
            List<GameInstanceConfiguration> priceContainingConfigs) {
        return getStrategy().adapt(priceContainingConfigs, params, PARAMS_KEY,
                (WgmfGameParams wgmfParams, GameInstanceConfiguration instanceConfig) ->
                        WhoGetsMyFlexGame
                                .createVariableDSOPricingGame(wgmfParams, instanceConfig.getSeed(),
                                        instanceConfig.getExtraConfigValues()
                                                .get(PRICE_PARAM_KEY)));
    }

    protected final List<GameInstanceConfiguration> getConfigsWithPricesFromDirector(double price,
            ConfigurableGameDirector director) {
        return director.getPlayableVersions().stream()
                .map((config) -> config.withExtraConfigValue(PRICE_PARAM_KEY, price))
                .collect(Collectors.toList());
    }

    @Override
    protected void processResults() {
        List<CsvResultWriter.WgmfDynamicsResults> toWrite = Lists.newArrayList();
        try (EgtResultParser egtResultParser = new EgtResultParser(null)) {
            for (Map.Entry<Double, ConfigurableGameDirector> entry : priceToDirector.entrySet()) {
                parseDynamicsAndAddToResults(entry.getKey(), entry.getValue(), toWrite,
                        egtResultParser);
            }
            CsvResultWriter.writeCsvFile(
                    RES_OUTPUT_FILE + String.valueOf(getnAgents()) + "R" + String
                            .valueOf(getnReps()) + "_" + String
                            .valueOf(System.currentTimeMillis() / 100), toWrite, false);
        } catch (Exception e) {
            logger.error("Something went wrong parsing the results", e);
            throw new RuntimeException(e);
        }
    }

    protected final void parseDynamicsAndAddToResults(double pricePoint,
            ConfigurableGameDirector director, List<CsvResultWriter.WgmfDynamicsResults> results,
            EgtResultParser egtResultParser) {
        EvolutionaryGameDynamics dynamics = EvolutionaryGameDynamics
                .from(director.getResults().getResults());
        ConfidenceInterval ciExt = director.getResults().getResults().getExternalityCI(CI_LEVEL);
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
        logger.warn("Results for pricepoint {}:", pricePoint);
        logger.warn("Dynamics equation params: {}",
                director.getDynamicEquationArguments());
        logger.warn("Phase plot fixed points found at: {}", Arrays.toString(fixedPoints));
        logger.warn("{} CI Lower bound Phase plot fixed points found at: {}",
                CI_LEVEL.getConfidenceLevel(), Arrays.toString(fixedPointsLower));
        logger.warn("{} CI Upper bound Phase plot fixed points found at: {}",
                CI_LEVEL.getConfidenceLevel(), Arrays.toString(fixedPointsHigher));
        String[] splitted = DATAPROFILE_TEMPLATE.split("/");

        results.add(CsvResultWriter.WgmfDynamicsResults
                .create(getnAgents(), getnReps(),
                        splitted[splitted.length - 1]
                                .replace("*", String.valueOf("[" + dataProfileIdx + "]")),
                        pricePoint,
                        fixedPoints,
                        fixedPointsLower,
                        fixedPointsHigher, eqnParams, lowerCI, higherCI,
                        CI_LEVEL.getConfidenceLevel(), ciExt, windErrorFileIdx));
    }

    protected static final List<Double> getHigherCIParams(EvolutionaryGameDynamics dynamics) {
        return getGenericCIParams(dynamics, ConfidenceInterval::getUpperBound,
                ConfidenceInterval::getLowerBound);
    }

    protected static final List<Double> getLowerCIParams(EvolutionaryGameDynamics dynamics) {
        return getGenericCIParams(dynamics, ConfidenceInterval::getLowerBound,
                ConfidenceInterval::getUpperBound);
    }

    private static List<Double> getGenericCIParams(EvolutionaryGameDynamics dynamics,
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

    protected double getMinPrice() {
        return minPrice;
    }

    protected double getMaxPrice() {
        return maxPrice;
    }

    protected double getPriceStep() {
        return priceStep;
    }

    protected int getnAgents() {
        return nAgents;
    }

    protected int getnReps() {
        return nReps;
    }
}