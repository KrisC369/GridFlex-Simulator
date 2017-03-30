package be.kuleuven.cs.gridflex.io;

/**
 * Interface representing an instance capable of serving up results that can be
 * written to an output channel.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
public interface Writable {
    /**
     * Return the formatted string for the results that are to be written.
     *
     * @return A string containing the results.
     */
    String getFormattedResultString();
}
