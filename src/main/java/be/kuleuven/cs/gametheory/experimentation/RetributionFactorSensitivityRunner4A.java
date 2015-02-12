package be.kuleuven.cs.gametheory.experimentation;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class RetributionFactorSensitivityRunner4A extends
        RetributionFactorSensitivityRunner {

    private static final int NAGENTS = 4;
    private static final int REPITITIONS = 30;
    private static final String TAG = "RESULT4A";

    protected RetributionFactorSensitivityRunner4A() {
        super(REPITITIONS, NAGENTS, TAG);
    }

    /**
     * Runs some experiments as a PoC.
     *
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        new RetributionFactorSensitivityRunner4A().execute();

    }
}
