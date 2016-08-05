package be.kuleuven.cs.flexsim.experimentation.runners;

/**
 * Callback interface for signaling when an execution is finished.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */@FunctionalInterface
public interface ExperimentCallback {
    /**
     * the callback method.
     *
     * @param instance
     *            the callee.
     */
    void callback(ExperimentAtom instance);

}
