package be.kuleuven.cs.gametheory;

/**
 * Generates game instances.
 *
 * @param <N> The agent type.
 * @param <S> The action type.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface GameInstanceGenerator<N, S> {
    /**
     * Generates an instance of a game.
     *
     * @return the game instance.
     */
    GameInstance<N, S> generateInstance();

    /**
     * Returns the size of the action of the generated instances.
     *
     * @return the size of the action set.
     * @deprecated GameInstances can offer the set of actions and their sizes.
     * Users should not rely on this method that can be implemented differently
     */
    @Deprecated
    int getActionSpaceSize();
}
