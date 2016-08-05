package be.kuleuven.cs.flexsim.experimentation.swift;

/**
 * An experiment instance that is executable.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */@FunctionalInterface
public interface ExecutableExperiment {
    /**
     * Execute the experiment.
     */
    void execute();
}
