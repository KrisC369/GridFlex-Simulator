package be.kuleuven.cs.gametheory;

/**
 * This class represents an abstraction for game theoretic agents to be used in
 * a game context.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The type of agent.
 *
 */
public interface Agent<T> {
    /**
     * Returns the concrete agent.
     * 
     * @return the agent.
     */
    T getConcreteAgent();
}
