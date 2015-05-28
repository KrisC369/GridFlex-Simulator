package be.kuleuven.cs.flexsim.experimentation.techReport;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class RetributionFactorSensitivityRunner3A extends
        RetributionFactorSensitivityRunner {

    private static final int NAGENTS = 3;
    private static final int REPITITIONS = 200;
    private static final String TAG = "RESULT3A";

    protected RetributionFactorSensitivityRunner3A() {
        super(REPITITIONS, NAGENTS, TAG);
    }

    /**
     * Runs some experiments as a PoC.
     * 
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        new RetributionFactorSensitivityRunner3A().execute();
    }
}
