package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfSingleScenTest {
    static final double TO_POWER = 1.73 * 15.6;
    static final double CONVERSION = 1.5d;
    static final double SLOTS_PER_HOUR = 4;
    private static final String executionMode = "LOCAL";
    private ExperimentParams experimentParams;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    @Ignore
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
                "-n", "4", "-r", "1", "-s", solver, "-m", executionMode, "-p1start", "12",
                "-p1step", "1", "-p1end", "13", "-pIdx", "0", "-dIdx", "5", "-distribution",
                "CAUCHY", "-flexIA", "12", "-flexDUR", "2", "-flexCOUNT", "40", "-flexIA", "12",
                "-flexDUR", "2", "-flexCOUNT", "40" };
    }
}