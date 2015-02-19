package be.kuleuven.cs.gametheory.experimentation;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class RetributionFactorSensitivityRunner6A extends
        RetributionFactorSensitivityRunner {

    private static final int NAGENTS = 6;
    private static final int REPITITIONS = 10;
    private static final String TAG = "RESULT6A";

    protected RetributionFactorSensitivityRunner6A() {
        super(REPITITIONS, NAGENTS, TAG);
    }

    /**
     * Runs some experiments as a PoC.
     *
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        new RetributionFactorSensitivityRunner6A().execute();
    }
}
