package be.kuleuven.cs.gametheory.experimentation.runners;

/**
 * Represents the smallest unit of sequential computation. This is a
 * representation of an atomic task to be exucted.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface ExperimentAtom extends Runnable {

    /**
     * Register a callback instance to call when experiment finishes running.
     *
     * @param c
     *            The callback instance to call.
     */
    void registerCallbackOnFinish(ExperimentCallback c);
}
