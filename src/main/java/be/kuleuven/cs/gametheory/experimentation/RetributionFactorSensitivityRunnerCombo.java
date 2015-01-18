package be.kuleuven.cs.gametheory.experimentation;

/**
 * Runs a combination of experiments for 2A, 3A and 4A
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class RetributionFactorSensitivityRunnerCombo {
    private static final int THREADS = 3;

    /**
     * Run it.
     * 
     * @param args
     *            the args.
     */
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MultiThreadedExperimentRunner(
                        new RetributionFactorSensitivityRunner2A(), THREADS)
                        .runExperiments();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MultiThreadedExperimentRunner(
                        new RetributionFactorSensitivityRunner3A(), THREADS)
                        .runExperiments();

            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MultiThreadedExperimentRunner(
                        new RetributionFactorSensitivityRunner4A(), THREADS)
                        .runExperiments();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MultiThreadedExperimentRunner(
                        new RetributionFactorSensitivityRunner5A(), THREADS)
                        .runExperiments();
            }
        }).start();
    }
}
