package be.kuleuven.cs.gridflex.protocol;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The concrete responder type parameter.
 */
public interface Initiator<T> {

    /**
     * @param r
     *            The responder
     */
    void registerResponder(Responder<T> r);
}
