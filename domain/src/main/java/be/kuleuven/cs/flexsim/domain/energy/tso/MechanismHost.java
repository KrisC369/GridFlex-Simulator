package be.kuleuven.cs.flexsim.domain.energy.tso;

/**
 * Models an abstract interaction mechanism of any kind.
 *
 * @param <T> The type of participant
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
public interface MechanismHost<T> {
    /**
     * Register a mechanism client/participant to this mechanism.
     *
     * @param participant The participant to register.
     */
    void registerParticipant(T participant);
}
