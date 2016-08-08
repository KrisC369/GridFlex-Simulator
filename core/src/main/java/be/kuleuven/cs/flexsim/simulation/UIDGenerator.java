package be.kuleuven.cs.flexsim.simulation;

/**
 * Generates unique IDs.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
public interface UIDGenerator {
    /**
     * Return next unique id.
     *
     * @return a long that is unique from previous ids.
     */
    long getNextUID();
}
