package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gametheory.configurable.GameInstanceConfiguration;
import be.kuleuven.cs.gametheory.configurable.GameInstanceResult;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.ErrorDistributionType;
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

import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.ExecutionStrategy.LOCAL;
import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.ExecutionStrategy.REMOTE;
import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.WgmfGameRunner.loadResources;
import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfJppfTaskTest {
    private static final int SEED = 3722;
    private static final String DISTRIBUTIONFILE = "windspeedDistributions.csv";
    private static final String POWERDISTRIBUTION = "powerDistributionsTestFile.csv";
    private static final String DATAFILE = "test.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices_short.csv";
    private static final String DAM_COLUMN = "damhp";
    private static final String DAMPRICES_DAILY = "dailyDayAheadPrices.csv";
    private static final String DB_WRITE_FILE_LOCATION = "persistence/write/testDB.db";
    private static final String PERSISTENCE_TEST_DB_DB = "persistence/testDB.db";
    private WgmfJppfTask task;
    private final String PARAMS = "test";
    private ExperimentParams expP;
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
            dataIn = WindBasedInputData.loadFromResource(DATAFILE, "test", "test");
            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(IMBAL);
            WindSpeedForecastMultiHorizonErrorDistribution windDist =
                    WindSpeedForecastMultiHorizonErrorDistribution
                            .loadFromCSV(DISTRIBUTIONFILE);
            PowerForecastMultiHorizonErrorDistribution powerDist =
                    PowerForecastMultiHorizonErrorDistribution
                            .loadFromCSV(POWERDISTRIBUTION);
            DayAheadPriceProfile dayAheadPriceProfile = DayAheadPriceProfile
                    .extrapolateFromHourlyOneDayData(DAMPRICES_DAILY, DAM_COLUMN, 7);

            WgmfGameParams params = WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(
                                    Solvers.TYPE.DUMMY, false, () -> null), specs,
                            windDist, powerDist, imbalIn, dayAheadPriceProfile,
                            ErrorDistributionType.NORMAL);
            GameInstanceConfiguration config = GameInstanceConfiguration.builder().setAgentSize(3)
                    .setActionSize(2)
                    .fixAgentToAction(0, 0).fixAgentToAction(1, 0).fixAgentToAction(2, 1)
                    .setSeed(231L).build();

            this.task = new WgmfJppfTask(config, params,
                    (WgmfGameParams wgmfParams, GameInstanceConfiguration conf) -> WhoGetsMyFlexGame
                            .createBasicGame(wgmfParams, conf.getSeed()));
            this.expP = ExperimentParams.builder().setNAgents(2).setNRepititions(1).setSolver(
                    Solvers.TYPE.DUMMY)
                    .setRemoteExecutable(true).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        GameInstanceResult result = task.getResult();
        assertEquals(0, result.getPayoffs().get(0), 0);
    }

    @Test
    public void testRunLocal() {
        WgmfGameRunner gameJPPFRunner = new WgmfGameRunner(expP, LOCAL);
        WgmfGameParams params = loadResources(expP);
        gameJPPFRunner.execute(params);
    }

    @Test
    public void testRunRemote() {
        WgmfGameRunner gameJPPFRunner = new WgmfGameRunner(expP, REMOTE);
        WgmfGameParams params = loadResources(expP);
        gameJPPFRunner.execute(params);
    }

}