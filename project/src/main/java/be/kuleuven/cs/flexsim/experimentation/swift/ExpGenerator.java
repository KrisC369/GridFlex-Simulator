package be.kuleuven.cs.flexsim.experimentation.swift;

/**
 * Generator for experiment instances. A builder interface.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */@FunctionalInterface
public interface ExpGenerator {

    /**
     * Returns an experiment executable file.
     * 
     * @param reps
     *            The amount of repititions.
     * @param agents
     *            The number of agents involved.
     * @param allowed
     *            The number of allowed excess.
     * @return an experiment executable file
     */
    ExecutableExperiment getExperiment(int reps, int agents, int allowed);
}
