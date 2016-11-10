package be.kuleuven.cs.gametheory;

import java.util.List;
import java.util.Map;

/**
 * Represents an instance of a game in a certain configuration generating one
 * entry in a payoff table.
 *
 * @param <N> The type of agents.
 * @param <K> The type of actions.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface GameInstance<N, K> extends Playable {
    /**
     * Get the payoffs for this configuration of the game.
     * Will be called post @link{Playable.play()}
     *
     * @return The payoff vector.
     */
    Map<N, Double> getPayOffs();

    /**
     * Fix a certain agent to a action. Call for every agent before calling
     * This method is called before @link{init()}
     *
     * @param agent  The agent.
     * @param action The action to fix the agent to.
     * @see{GameInstance.init or @see{GameInstance.play}.
     */
    void fixActionToAgent(N agent, K action);

    /**
     * Initialize the instance components.
     */
    void init();

    /**
     * Returns the set of actions available to agents.
     *
     * @return list of actions.
     */
    List<K> getActionSet();

    /**
     * Returns the map from agents to actions.
     *
     * @return the mapping.
     */
    Map<N, K> getAgentToActionMapping();

    /**
     * Returns the value for the externality of this game instance. This
     * externality can be positive or negative.
     *
     * @return positive or negative externality for this game.
     */
    long getExternalityValue();
}
