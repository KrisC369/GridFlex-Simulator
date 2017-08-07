package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.gridflex.domain.util.data.PowerForecastMultiHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.WindSpeedForecastMultiHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.WindBasedInputData;
import org.slf4j.Logger;

import java.io.IOException;

import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.WgmfInputParser.parseInputAndExec;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Abstract game runner. Extend this for variations on similar experiments.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *///TODO REFACTOR all non-jppf out of this package.
public abstract class AbstractWgmfGameRunner {
    public static final String WIND_DISTRIBUTIONFILE_TEMPLATE =
            "be/kuleuven/cs/gridflex/experimentation/data/windspeedDistributions*.csv";
    public static final String POWER_DISTRIBUTIONFILE_TEMPLATE =
            "be/kuleuven/cs/gridflex/experimentation/data/powerDistributions*.csv";
    public static final String DATAPROFILE_TEMPLATE =
            "be/kuleuven/cs/gridflex/experimentation/data/currentAndCongestionProfile*.csv";
    private static final String SPECFILE =
            "be/kuleuven/cs/gridflex/experimentation/data/specs_enercon_e101-e1.csv";
    private static final String IMBAL =
            "be/kuleuven/cs/gridflex/experimentation/data/imbalance_prices.csv";
    private static final String DAM_COLUMN = "damhp";
    private static final String DAMPRICES_DAILY =
            "be/kuleuven/cs/gridflex/experimentation/data/dailyDayAheadPrices.csv";
    private static final int FULL_YEAR = 365;
    private static final Logger logger = getLogger(AbstractWgmfGameRunner.class);
    private static final String DB_FILE_LOCATION = "persistence/memoDB.db";
    private static final String DB_WRITE_FILE_LOCATION = "persistence/write/memoDB.db";
    protected static final String PARAMS_KEY = "PARAMS_KEY";
    protected static final int ACTION_SIZE = 2;

    private final ExecutionStrategy strategy;

    public AbstractWgmfGameRunner(ExecutionStrategy strategy) {
        this.strategy = strategy;
    }

    public static WgmfGameParams loadResources(ExperimentParams expP) {
        try {
            String dataFile = parseDataFileName(expP.getCurrentDataProfileIndex(),
                    DATAPROFILE_TEMPLATE);
            WindBasedInputData dataIn = WindBasedInputData.loadFromResource(dataFile);

            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);

            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);

            String windSpeedDistFile = parseDataFileName(expP.getWindErrorProfileIndex(),
                    WIND_DISTRIBUTIONFILE_TEMPLATE);
            WindSpeedForecastMultiHorizonErrorDistribution windDistribution =
                    WindSpeedForecastMultiHorizonErrorDistribution
                            .loadFromCSV(windSpeedDistFile);

            String powerDistFile = parseDataFileName(expP.getWindErrorProfileIndex(),
                    POWER_DISTRIBUTIONFILE_TEMPLATE);
            PowerForecastMultiHorizonErrorDistribution powerDistribution =
                    PowerForecastMultiHorizonErrorDistribution
                            .loadFromCSV(powerDistFile);

            DayAheadPriceProfile dayAheadPriceProfile = DayAheadPriceProfile
                    .extrapolateFromHourlyOneDayData(DAMPRICES_DAILY, DAM_COLUMN, FULL_YEAR);

            WgmfMemContextFactory memContext = new WgmfMemContextFactory(expP.isCachingEnabled(),
                    expP.isCacheExistenceEnsured(), DB_FILE_LOCATION, DB_WRITE_FILE_LOCATION);
            return WgmfGameParams.create(dataIn,
                    new WgmfSolverFactory(expP.getSolver(), expP.isUpdateCacheEnabled(),
                            memContext), specs, windDistribution, powerDistribution, imbalIn,
                    dayAheadPriceProfile, expP.getDistribution());
        } catch (IOException e) {
            throw new IllegalStateException("One of the resources could not be loaded.", e);
        }
    }

    static String parseDataFileName(int expPIndex,
            String distributionfileTemplate) {
        String dFile = distributionfileTemplate;
        final int idx = expPIndex;
        if (idx < 0) {
            dFile = dFile.replace("*", "");
        } else {
            dFile = dFile.replace("*", "[" + idx + "]");
        }
        return dFile;
    }

    public static void startExecution(String[] args, RunnerFactory factory) {
        ExperimentParams expP = parseInputAndExec(args);
        AbstractWgmfGameRunner gameJPPFRunner = factory.getRunner(expP);
        WgmfGameParams params = loadResources(expP);
        gameJPPFRunner.execute(params);
        gameJPPFRunner.processResults();
    }

    ExecutionStrategy getStrategy() {
        return strategy;
    }

    protected abstract void execute(WgmfGameParams params);

    protected abstract void processResults();

    @FunctionalInterface
    protected interface RunnerFactory {
        AbstractWgmfGameRunner getRunner(ExperimentParams params);
    }
}
