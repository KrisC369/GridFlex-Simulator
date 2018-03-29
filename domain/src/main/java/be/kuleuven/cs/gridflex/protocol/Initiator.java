package be.kuleuven.cs.gridflex.protocol;

/**
 * @param <T> The concrete responder type parameter.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
public interface Initiator<T> {

    /**
     * @param r The responder
     */
    void registerResponder(Responder<T> r);
}
