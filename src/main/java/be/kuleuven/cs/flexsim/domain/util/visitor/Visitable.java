package be.kuleuven.cs.flexsim.domain.util.visitor;

/**
 * Interface representing an entity that can be visited by a visitor.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The type of subject to register with.
 */
public interface Visitable<T> {
    /**
     * Register this entity with a registerable instance.
     * 
     * @param visitor
     *            the subject to register with.
     */
    void acceptVisitor(T visitor);
}
