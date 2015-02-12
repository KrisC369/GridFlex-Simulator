package be.kuleuven.cs.gametheory.experimentation.runners;

import be.kuleuven.cs.gametheory.experimentation.runners.local.MultiThreadedExperimentRunner;
import be.kuleuven.cs.gametheory.experimentation.runners.local.SingleThreadedExperimentRunner;

/**
 * Factory for creating experiment Runners.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class RunnerFactory {
    private static final int DEFAULT_THREADS = 4;

    private RunnerFactory() {
    };

    /**
     * Creates a blocking multi-threaded runner.
     *
     * @return The specified ExperimentRunner.
     */
    public static ExperimentRunner createDefaultMultiThreadedRunner() {
        return new MultiThreadedExperimentRunner(DEFAULT_THREADS);
    }

    /**
     * Creates a blocking single threaded runner.
     *
     * @return The specified ExperimentRunner.
     */
    public static ExperimentRunner createDefaultSingleThreadedRunner() {
        return new SingleThreadedExperimentRunner();
    }

    /**
     * Creates a multiprocessor runner.
     *
     * @param availableProcs
     *            the amount of threads to have simultaneously.
     * @return The specified ExperimentRunner.
     */
    public static ExperimentRunner createCustomMultiThreadedRunner(
            int availableProcs) {
        return new MultiThreadedExperimentRunner(availableProcs);
    }
}
