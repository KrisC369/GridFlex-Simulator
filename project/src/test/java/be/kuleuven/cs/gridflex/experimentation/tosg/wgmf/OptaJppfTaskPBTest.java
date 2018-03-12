package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.ErrorDistributionType;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.gridflex.domain.util.data.PowerForecastMultiHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.WindSpeedForecastMultiHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.WindBasedInputData;
import be.kuleuven.cs.gridflex.solvers.Solvers;
import org.jppf.server.JPPFDriver;
import org.jppf.utils.JPPFConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.WgmfGameRunner.loadResources;
import static be.kuleuven.cs.gridflex.solvers.Solvers.TYPE.DUMMY;
import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class OptaJppfTaskPBTest {
    private static final int SEED = 3722;
    private static final String DISTRIBUTIONFILE = "windspeedDistributions.csv";
    private static final String POWERDISTRIBUTION = "powerDistributionsTestFile.csv";
    private static final String DATAFILE =
            "be/kuleuven/cs/gridflex/experimentation/data/currentAndCongestionProfile[0].csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices.csv";
    private static final String DAM_COLUMN = "damhp";
    private static final String DAMPRICES_DAILY = "dailyDayAheadPrices.csv";
    private static final String DB_WRITE_FILE_LOCATION = "persistence/write/testDB.db";
    private static final String PERSISTENCE_TEST_DB_DB = "persistence/testDB.db";
    private OptaJppfTaskPB task;
    private final String PARAMS = "test";
    private ExperimentParams expP;
    private final Solvers.TYPE solvertype = DUMMY;
    @SuppressWarnings("null")
    private static JPPFDriver driver;

    @BeforeClass
    public static void setUpClass() {
        JPPFConfiguration.getProperties().setBoolean("jppf.local.node.enabled",
                true);
        JPPFConfiguration.getProperties().setBoolean("jppf.discovery.enabled",
                false);
        JPPFDriver.main(new String[] { "noLauncher" });
        driver = JPPFDriver.getInstance();
    }

    @Before
    public void setUp() {
        WindBasedInputData dataIn = null;
        try {
            dataIn = WindBasedInputData.loadFromResource(DATAFILE);
            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);
            WindSpeedForecastMultiHorizonErrorDistribution windDist =
                    WindSpeedForecastMultiHorizonErrorDistribution
                            .loadFromCSV(DISTRIBUTIONFILE);
            PowerForecastMultiHorizonErrorDistribution powerDist =
                    PowerForecastMultiHorizonErrorDistribution
                            .loadFromCSV(POWERDISTRIBUTION);
            DayAheadPriceProfile dayAheadPriceProfile = DayAheadPriceProfile
                    .extrapolateFromHourlyOneDayData(DAMPRICES_DAILY, DAM_COLUMN, 365);

            WgmfGameParams params = WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(
                                    solvertype, false, () -> null), specs,
                            windDist, powerDist, imbalIn, dayAheadPriceProfile,
                            ErrorDistributionType.CAUCHY, HourlyFlexConstraints.R3DP);

            this.task = new OptaJppfTaskPB(params, 1000, 8,
                    HourlyFlexConstraints.R3DP);
            setExp(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setExp(boolean remote) {
        this.expP = ExperimentParams.builder().setNAgents(8).setNRepititions(1).setSolver(
                solvertype)
                .setRemoteExecutable(remote).build();
    }

    /**
     * Stops the JPPF driver.
     */
    @AfterClass
    public static void tearDown() {
        driver.shutdown();
    }

    @Test
    public void testRun() {
        task.run();
        OptaExperimentResults result = task.getResult();
        assertEquals(-1, result.getResolvedCongestionValue().doubleValue(), 0);
    }

    @Test
    public void testRunLocal() {
        setExp(false);
        AbstractWgmfGameRunner gameJPPFRunner = new WgmfMultiJobGameRunnerVariableFlexParams(expP);
        WgmfGameParams params = loadResources(expP);
        gameJPPFRunner.execute(params);
    }

    @Test
    public void testRunRemote() {
        setExp(true);
        AbstractWgmfGameRunner gameJPPFRunner = new WgmfMultiJobGameRunnerVariableFlexParams(expP);
        WgmfGameParams params = loadResources(expP);
        gameJPPFRunner.execute(params);
    }

}