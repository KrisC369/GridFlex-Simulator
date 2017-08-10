package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.EgtCsvResultWriter;
import be.kuleuven.cs.gridflex.experimentation.tosg.stat.EgtResultParser;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGame;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirector;
import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.map.UnmodifiableMap;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Game runner for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class  WgmfMultiJobGameRunnerVariableDistributionCosts
        extends WgmfGameRunnerVariableDistributionCosts {
    private static final Logger logger = getLogger(
            WgmfMultiJobGameRunnerVariableDistributionCosts.class);
    private final LinkedListMultimap<ConfigurableGameDirector, WgmfJppfTask> directorToTasks;
    private final List<EgtCsvResultWriter.WgmfDynamicsResults> writableResults;
    private final String resultFileName;

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP The experiment parameters.
     */
    private WgmfMultiJobGameRunnerVariableDistributionCosts(ExperimentParams expP) {
        super(expP);
        directorToTasks = LinkedListMultimap.create();
        writableResults = Lists.newArrayList();
        resultFileName =
                RES_OUTPUT_FILE + String.valueOf(getnAgents()) + "R" + String.valueOf(getnReps())
                        + "_" + String.valueOf(System.currentTimeMillis() / 100) + RES_EXTENSION;
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        startExecution(args,
                WgmfMultiJobGameRunnerVariableDistributionCosts::new);
    }

    @Override
    protected void execute(WgmfGameParams params) {
        EgtCsvResultWriter.writeCsvFile(resultFileName, Collections.emptyList(), false);
        for (double price = getMinPrice(); price <= getMaxPrice(); price += getPriceStep()) {
            List<WgmfJppfTask> alltasks = Lists.newArrayList();

            ConfigurableGame game = new ConfigurableGame(getnAgents(),
                    ACTION_SIZE, getnReps());
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
                    UnmodifiableMap.unmodifiableMap(priceToDirector));
            processSingleResult(price, director);
        }

    }

    private void processSingleResult(Double price, ConfigurableGameDirector d) {
        try (EgtResultParser egtResultParser = new EgtResultParser(null)) {
            parseDynamicsAndAddToResults(price, d, writableResults, egtResultParser);
            EgtCsvResultWriter.writeCsvFile(resultFileName,
                    writableResults.subList(writableResults.size() - 1, writableResults.size()),
                    true);
        } catch (Exception e) {
            logger.error("Something went wrong parsing the results", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void processResults() {
        EgtCsvResultWriter.writeCsvFile(resultFileName + ".whole", writableResults, false);
    }
}