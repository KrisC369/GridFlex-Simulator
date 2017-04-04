package be.kuleuven.cs.gridflex.experimentation.runners;

import java.util.concurrent.Callable;

/**
 * Represents the smallest unit of sequential computation. This is a
 * representation of an atomic task to be exucted.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface ExperimentAtom extends Callable<Object> {

    /**
     * Register a callback instance to call when experiment finishes running.
     *
     * @param c The callback instance to call.
     */
    void registerCallbackOnFinish(ExperimentCallback c);
}
