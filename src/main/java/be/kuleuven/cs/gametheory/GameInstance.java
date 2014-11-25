package be.kuleuven.cs.gametheory;

import java.util.Map;

/**
 * Represents an instance of a game in a certain configuration generating one
 * entry in a payoff table.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <N>
 *            The type of agents.
 * @param <K>
 *            The type of actions.
 *
 */
public interface GameInstance<N, K> {
    /**
     * Get the payoffs for this configuration of the game.
     * 
     * @return The payoff vector.
     */
    Map<Agent<N>, Long> getPayOffs();

    /**
     * Fix a certain agent to a action. Call for every agent.
     * 
     * @param agent
     *            The agent.
     * @param action
     *            The action to fix the agent to.
     */
    void fixActionToAgent(N agent, K action);

    /**
     * Start the simulation.
     */
    void start();

    /**
     * Initialize the simulation components.
     */
    void init();
}
