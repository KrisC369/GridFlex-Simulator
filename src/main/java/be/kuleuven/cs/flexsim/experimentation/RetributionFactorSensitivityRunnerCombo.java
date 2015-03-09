package be.kuleuven.cs.flexsim.experimentation;

/**
 * Runs a combination of experiments for 2A, 3A and 4A.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class RetributionFactorSensitivityRunnerCombo {

    private RetributionFactorSensitivityRunnerCombo() {
    }

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
                new RetributionFactorSensitivityRunner2A().execute();

            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new RetributionFactorSensitivityRunner3A().execute();

            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new RetributionFactorSensitivityRunner4A().execute();

            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new RetributionFactorSensitivityRunner5A().execute();

            }
        }).start();
    }
}
