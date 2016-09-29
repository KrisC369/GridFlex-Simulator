package be.kuleuven.cs.flexsim.experimentation.runners.jppf;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.runners.local.MultiThreadedExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.runners.local.SingleThreadedExperimentRunner;

/**
 * Factory for creating experiment Runners.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class RemoteRunners {
    private static final int DEFAULT_THREADS = 4;

    private RemoteRunners() {
    }

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
            final int availableProcs) {
        return new MultiThreadedExperimentRunner(availableProcs);
    }

    /**
     * Creates a multiprocessor runner with as many threads in the pool as there
     * are cores available.
     *
     * @return The specified ExperimentRunner.
     */
    public static ExperimentRunner createOSTunedMultiThreadedRunner() {
        return new MultiThreadedExperimentRunner(
                Runtime.getRuntime().availableProcessors());
    }
}
