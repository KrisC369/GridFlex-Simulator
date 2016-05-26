package be.kuleuven.cs.gametheory;

/**
 * Represents an action in a game theoretic context. Agents can choose these
 * type of actions.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The type for the action target.
 */
public interface Action<T> {
    /**
     * Returns the target for this action. Eg. if this target is a computation
     * strategy, it returns this strategy.
     * 
     * @return The target.
     */
    T getTarget();
}
