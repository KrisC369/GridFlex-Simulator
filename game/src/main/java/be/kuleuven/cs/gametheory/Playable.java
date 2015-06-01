package be.kuleuven.cs.gametheory;

/**
 * Represents a playable game variation meant to set or determine payoffs for a
 * specific variation of a specific game.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface Playable {
    /**
     * Play the game.
     */
    void play();
}
