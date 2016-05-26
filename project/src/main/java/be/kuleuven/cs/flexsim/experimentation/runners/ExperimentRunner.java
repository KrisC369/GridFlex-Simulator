package be.kuleuven.cs.flexsim.experimentation.runners;

import java.util.Collection;

/**
 * This component is able to execute a series of task experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface ExperimentRunner {
    /**
     * Run the experiments.
     *
     * @param experiments
     *            The experiment collection to take tasks from.
     */
    void runExperiments(Collection<? extends ExperimentAtom> experiments);

    /**
     * Returns true if this runner is performing work. Returns false if done.
     *
     * @return true if this runner is performing work. Returns false if done.
     */
    boolean isRunning();
}
