package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.DistributionGridCongestionSolver;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.jppf.server.JPPFDriver;
import org.jppf.utils.JPPFConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf
        .WgmfGameRunnerVariableDistributionCostsTest.loadTestResources;
import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf
        .WgmfGameRunnerVariableDistributionCostsTest.printAllocations;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunnerVariableFlexParamsTest {
    static final double TO_POWER = 1.73 * 15.6;
    static final double CONVERSION = 1.5d;
    static final double SLOTS_PER_HOUR = 4;
    private static final String executionMode = "LOCAL";
    private static JPPFDriver driver;
    private ExperimentParams experimentParams;

    @BeforeClass
    public static void setUpClass() {
        if ("REMOTE".equalsIgnoreCase(executionMode)) {
            JPPFConfiguration.getProperties().setBoolean("jppf.local.node.enabled",
                    true);
            JPPFConfiguration.getProperties().setBoolean("jppf.discovery.enabled",
                    false);
            JPPFDriver.main(new String[] { "noLauncher" });
            driver = JPPFDriver.getInstance();
        }
    }

    /**
     * Stops the JPPF driver.
     */
    @AfterClass
    public static void tearDown() {
        if ("REMOTE".equalsIgnoreCase(executionMode)) {
            driver.shutdown();
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    //    @Ignore
    public void main() throws Exception {
        //        WgmfGameRunnerVariableDistributionCosts.main(getArgLine("OPTA"));
        WgmfMultiJobGameRunnerVariableFlexParams.main(getArgLine("DUMMY"));
    }

    @Test
    @Ignore
    public void testDSOAllocationResultsSmallTest() {
        experimentParams = getParams("OPTA");
        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams,
                "be/kuleuven/cs/gridflex/experimentation/data/imbalance_prices.csv",
                "smalltest.csv",
                "test", "test", 1);

        DistributionGridCongestionSolver dgcSolver = new
                DistributionGridCongestionSolver(
                wgmfGameParams.getFactory(),
                wgmfGameParams.getInputData().getCongestionProfile());

        HourlyFlexConstraints flex = HourlyFlexConstraints.builder().activationDuration(1)
                .interActivationTime(1).maximumActivations(2).build();

        dgcSolver.registerFlexProvider(new FlexProvider(200, flex));
                dgcSolver.registerFlexProvider(new FlexProvider(500, flex));
        TimeSeries ts = dgcSolver
                .getCongestionVolumeToResolve();
        DoubleList toSolve = ts.values();
        DoubleList input = wgmfGameParams.getInputData().getCableCurrentProfile()
                .transform(p -> (p / CONVERSION) * TO_POWER)
                .transform(p -> p * CONVERSION / SLOTS_PER_HOUR).values();
        dgcSolver.solve();

        System.out.println(toSolve);
        System.out.println(input);
        System.out.println(
                "Solved: " + dgcSolver.getSolution().getObjectiveValue() + " / " + dgcSolver
                        .getSolution().getNormalizedObjectiveValue() + " % of optimal");
        ListMultimap<FlexibilityProvider, Boolean> allocationMaps = dgcSolver
                .getSolution().getAllocationMaps();
        printAllocations(allocationMaps);
    }

    public static ExperimentParams getParams(String solver) {
        return WgmfInputParser.parseInputAndExec(getArgLine(solver));
    }

    private static String[] getArgLine(String solver) {
        //        return new String[] {
        //                "-n", "2", "-r", "1", "-s", solver, "-m", "LOCAL", "-p1start", "35.4",
        // "-p1step",
        //                "10", "-p1end", "45.5", "-dIdx", "1", "-pIdx", "1" };
        return new String[] {
                "-n", "4", "-r", "1", "-s", solver, "-m", executionMode, "-p1start", "12",
                "-p1step", "1", "-p1end", "13", "-pIdx", "0", "-dIdx", "5", "-distribution",
                "CAUCHY", "-flexIA", "12", "-flexDUR", "2", "-flexCOUNT", "40" };
    }
}