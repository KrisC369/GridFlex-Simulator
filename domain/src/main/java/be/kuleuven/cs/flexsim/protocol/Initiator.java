package be.kuleuven.cs.flexsim.protocol;

/**
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *
 */
public interface Initiator<T> {

    /**
     * 
     * @param r
     */
    void registerResponder(Responder<T> r);
}
