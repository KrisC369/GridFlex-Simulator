package be.kuleuven.cs.gametheory;

/**
 * Generates game instances.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <N>
 *            The agent type.
 * @param <S>
 *            The action type.
 *
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
     */
    int getActionSpaceSize();
}
