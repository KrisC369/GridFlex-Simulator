package be.kuleuven.cs.gametheory;

import java.util.List;
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
public interface GameInstance<N, K> extends Playable {
    /**
     * Get the payoffs for this configuration of the game.
     *
     * @return The payoff vector.
     */
    Map<N, Long> getPayOffs();

    /**
     * Fix a certain agent to a action. Call for every agent before calling
     *
     * @see{GameInstance.init or @see{GameInstance.play}.
     *
     * @param agent
     *            The agent.
     * @param action
     *            The action to fix the agent to.
     */
    void fixActionToAgent(N agent, K action);

    /**
     * Initialize the simulation components.
     */
    void init();

    /**
     * Returns the set of actions available to agents.
     *
     * @return list of actions.
     */
    List<K> getActionSet();

    /**
     * Returns the map from sites to actions.
     *
     * @return the mapping.
     */
    Map<N, K> getAgentToActionMapping();
}
