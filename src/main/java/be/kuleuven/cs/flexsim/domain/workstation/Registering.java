package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * Interface representing an entity that can register themselves to a subject.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * @param <T>
 *            The type of subject to register with.
 */
public interface Registering<T> {
    /**
     * Register this entity with a registerable instance.
     * 
     * @param subject
     *            the subject to register with.
     */
    void registerWith(T subject);
}
