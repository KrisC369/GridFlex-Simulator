package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunnerVariableFlexParamsTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    @Ignore
    public void main() throws Exception {
        //        WgmfGameRunnerVariableDistributionCosts.main(getArgLine("OPTA"));
        WgmfMultiJobGameRunnerVariableFlexParams.main(getArgLine("OPTA"));
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
                "-n", "4", "-r", "10", "-s", solver, "-c", "ue", "-m", "LOCAL", "-p1start", "15.5",
                "-p1step",
                "10", "-p1end", "16.5", "-pIdx", "1", "-distribution", "CAUCHY", "-flexIA", "12",
                "-flexDUR", "2", "-flexCOUNT", "40" };
    }
}