package be.kuleuven.cs.gametheory;

/**
 * Generator for GT elements like an interface that represents an agent
 * generator.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The type of the generated element.
 */
public interface AgentGenerator<T> {
    /**
     * Return the generator's element.
     * 
     * @return The element.
     */
    T getAgent();
}
