package be.kuleuven.cs.gametheory;

/**
 * Generates game instances.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T1>
 *            The agent type.
 * @param <T2>
 *            The action type.
 *
 */
public interface GameInstanceGenerator<T1, T2> {
    /**
     * Generates an instance of a game.
     * 
     * @return the game instance.
     */
    GameInstance<T1, T2> generateInstance();

    /**
     * Returns the size of the action of the generated instances.
     * 
     * @return the size of the action set.
     */
    int getActionSpaceSize();
}
