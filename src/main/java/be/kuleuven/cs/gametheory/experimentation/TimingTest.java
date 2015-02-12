package be.kuleuven.cs.gametheory.experimentation;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class TimingTest extends RetributionFactorSensitivityRunner {

    private static final int NAGENTS = 2;
    private static final int REPITITIONS = 200;
    private static final String TAG = "RESULT2A";

    protected TimingTest() {
        super(REPITITIONS, NAGENTS, TAG);
    }

    /**
     * Runs some experiments as a PoC.
     *
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        long before1 = System.nanoTime();
        new RetributionFactorSensitivityRunner2A().execute();
        long after1 = System.nanoTime();
        new RetributionFactorSensitivityRunner2A().executeBatch();
        long after2 = System.nanoTime();

        System.out.println("regular: " + (after1 - before1) / 1000
                + " and Batch: " + (after2 - after1) / 1000);
    }
}
