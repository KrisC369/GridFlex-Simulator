package be.kuleuven.cs.gametheory.experimentation.runners;

/**
 * Represents an instance that serves as an experiment that can be run. with a
 * parameter.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface RunnableExperiment {
    /**
     * Method that triggers the run of an experiment with a specified parameter
     * depending on the context.
     *
     * @param varParam
     *            the parameter value to differentiate in.
     */
    void doExperimentRun(double varParam);

}
