package be.kuleuven.cs.gridflex.util.visitor;

/**
 * Interface representing an entity that can be visited by a visitor.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The type of subject to register with.
 */@FunctionalInterface
public interface Visitable<T> {
    /**
     * Register this entity with a registerable instance.
     * 
     * @param visitor
     *            the subject to register with.
     */
    void acceptVisitor(T visitor);
}
