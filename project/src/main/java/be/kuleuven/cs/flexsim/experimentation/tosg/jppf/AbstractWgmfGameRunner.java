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
 */
public abstract class AbstractWgmfGameRunner {
    private static final String DISTRIBUTIONFILE = "windspeedDistributions.csv";
    private static final String DATAFILE = "2kwartOpEnNeer.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices.csv";
    private static final String DAM_COLUMN = "damhp";
    private static final String DAMPRICES_DAILY = "dailyDayAheadPrices.csv";
    private static final Logger logger = getLogger(AbstractWgmfGameRunner.class);
    protected static final String PARAMS_KEY = "PARAMS_KEY";
    protected static final int ACTION_SIZE = 2;

    private final ExecutionStrategy strategy;

    public AbstractWgmfGameRunner(ExecutionStrategy strategy) {
        this.strategy = strategy;
    }

    public static WgmfGameParams loadResources(ExperimentParams expP) {
        try {
            WindBasedInputData dataIn = WindBasedInputData.loadFromResource(DATAFILE);
            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV(DISTRIBUTIONFILE);
            DayAheadPriceProfile dayAheadPriceProfile = DayAheadPriceProfile
                    .extrapolateFromHourlyOneDayData(DAMPRICES_DAILY, DAM_COLUMN);
            return WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(expP.getSolver()), specs, distribution,
                            imbalIn, dayAheadPriceProfile);
        } catch (IOException e) {
            throw new IllegalStateException("One of the resources could not be loaded.", e);
        }
    }

    public static void startExecution(String[] args, RunnerFactory factory) {
        ExperimentParams expP = parseInputAndExec(args);
        AbstractWgmfGameRunner gameJPPFRunner = factory.getRunner(expP);
        WgmfGameParams params = loadResources(expP);
        gameJPPFRunner.execute(params);
        gameJPPFRunner.logResults();
    }

    protected ExecutionStrategy getStrategy() {
        return strategy;
    }

    protected abstract void execute(WgmfGameParams params);

    protected abstract void logResults();

    @FunctionalInterface
    protected interface RunnerFactory {
        AbstractWgmfGameRunner getRunner(ExperimentParams params);
    }
}
