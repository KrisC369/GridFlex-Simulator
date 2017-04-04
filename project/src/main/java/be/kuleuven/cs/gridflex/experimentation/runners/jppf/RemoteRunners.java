package be.kuleuven.cs.gridflex.experimentation.runners.jppf;

import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentRunner;

import java.util.Map;

/**
 * Factory for creating remote experiment Runners.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class RemoteRunners {
    private RemoteRunners() {
    }

    /**
     * Creates a blocking jppf runner.
     *
     * @return The specified ExperimentRunner.
     */
    public static ExperimentRunner createDefaultBlockedJPPFRunner(String jobName,
            Map<String, Object> dataParams) {
        return new JPPFBlockingExperimentRunner(jobName, dataParams);
    }
}
