package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import org.jppf.server.JPPFDriver;
import org.jppf.utils.JPPFConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunnerVariableFlexParamsTest {
    private static final String executionMode = "LOCAL";
    private static JPPFDriver driver;

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

    public static ExperimentParams getParams(String solver) {
        return WgmfInputParser.parseInputAndExec(getArgLine(solver));
    }

    private static String[] getArgLine(String solver) {
        //        return new String[] {
        //                "-n", "2", "-r", "1", "-s", solver, "-m", "LOCAL", "-p1start", "35.4",
        // "-p1step",
        //                "10", "-p1end", "45.5", "-dIdx", "1", "-pIdx", "1" };
        return new String[] {
                "-n", "4", "-r", "1", "-s", solver, "-m", executionMode, "-p1start", "15.5",
                "-p1step",
                "10", "-p1end", "16.5", "-pIdx", "1", "-distribution", "CAUCHY", "-flexIA", "12",
                "-flexDUR", "2", "-flexCOUNT", "40" };
    }
}