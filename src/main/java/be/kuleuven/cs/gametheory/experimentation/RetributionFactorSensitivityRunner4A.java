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
    private static final int REPITITIONS = 15;
    private static final int THREADS = 3;

    protected RetributionFactorSensitivityRunner4A() {
        super(REPITITIONS, NAGENTS);
    }

    /**
     * Runs some experiments as a PoC.
     * 
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        new MultiThreadedExperimentRunner(
                new RetributionFactorSensitivityRunner4A(), THREADS)
                .runExperiments();
    }
}
