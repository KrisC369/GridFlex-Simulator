package be.kuleuven.cs.gametheory.experimentation.runners;

/**
 * Callback interface for signaling when an execution is finished.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface ExperimentCallback {
    /**
     * the callback method.
     *
     * @param instance
     *            the callee.
     */
    void callback(ExperimentAtom instance);

}
