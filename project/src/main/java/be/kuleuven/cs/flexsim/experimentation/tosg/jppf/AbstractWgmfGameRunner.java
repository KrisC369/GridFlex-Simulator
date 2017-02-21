package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.WindBasedInputData;
import org.slf4j.Logger;

import java.io.IOException;

import static be.kuleuven.cs.flexsim.experimentation.tosg.WgmfInputParser.parseInputAndExec;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Abstract game runner. Extend this for variations on similar experiments.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *///TODO REFACTOR all non-jppf out of this package.
public abstract class AbstractWgmfGameRunner {
    public static final String DISTRIBUTIONFILE_TEMPLATE =
            "be/kuleuven/cs/flexsim/experimentation/data/windspeedDistributions*.csv";
    public static final String DATAPROFILE_TEMPLATE =
            "be/kuleuven/cs/flexsim/experimentation/data/currentAndCongestionProfile*.csv";
//    protected static final String DATAFILE = "be/kuleuven/cs/flexsim/experimentation/data"
//            + "/2kwartOpEnNeer.csv";
    private static final String SPECFILE =
            "be/kuleuven/cs/flexsim/experimentation/data/specs_enercon_e101-e1.csv";
    private static final String IMBAL =
            "be/kuleuven/cs/flexsim/experimentation/data/imbalance_prices.csv";
    private static final String DAM_COLUMN = "damhp";
    private static final String DAMPRICES_DAILY =
            "be/kuleuven/cs/flexsim/experimentation/data/dailyDayAheadPrices.csv";
    private static final int FULL_YEAR = 365;
    private static final Logger logger = getLogger(AbstractWgmfGameRunner.class);
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

            String distFile = parseDataFileName(expP.getWindErrorProfileIndex(),
                    DISTRIBUTIONFILE_TEMPLATE);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV(distFile);

            DayAheadPriceProfile dayAheadPriceProfile = DayAheadPriceProfile
                    .extrapolateFromHourlyOneDayData(DAMPRICES_DAILY, DAM_COLUMN, FULL_YEAR);

            return WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(expP.getSolver()), specs, distribution,
                            imbalIn, dayAheadPriceProfile);
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
    interface RunnerFactory {
        AbstractWgmfGameRunner getRunner(ExperimentParams params);
    }
}
