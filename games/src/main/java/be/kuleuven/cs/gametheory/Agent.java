package be.kuleuven.cs.gametheory;

/**
 * This class represents an abstraction for game theoretic agents to be used in
 * a game context.
 *
 * @param <T> The type of agent.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
@Deprecated
public interface Agent<T> {
    /**
     * Returns the concrete agent.
     *
     * @return the agent.
     */
    T getConcreteAgent();
}
