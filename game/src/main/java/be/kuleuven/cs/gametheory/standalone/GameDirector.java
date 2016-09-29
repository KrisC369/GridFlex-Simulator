package be.kuleuven.cs.gametheory.standalone;

import be.kuleuven.cs.gametheory.AbstractGameDirector;
import be.kuleuven.cs.gametheory.GameInstance;
import be.kuleuven.cs.gametheory.Playable;

/**
 * This instance governs the sequential rules and protocols involved when
 * setting up and playing games.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class GameDirector<N, K>
        extends AbstractGameDirector<GameInstance<N, K>, GameInstance<N, K>> {

    /**
     * Default constructor.
     *
     * @param game The game to direct.
     */
    public GameDirector(final Game<N, K> game) {
        super(game);
    }

    /**
     * Call this method when an instance of the game has been played. This
     * method will perform post game-play clean-ups and hooks.
     *
     * @param finished The finished game variation.
     */
    public void notifyVersionHasBeenPlayed(final Playable finished) {
        final boolean successful = getInternalPlayables().remove(finished);
        if (!successful) {
            throw new IllegalArgumentException(
                    "The played instance does not occur in the current game.");
        }
        if (getPlayableVersions().isEmpty()) {
            runPostGame();
        }
    }

    private void runPostGame() {
        getGame().gatherResults(null);
        logResults();
    }

    /**
     * Play a full game specification autonomously.
     */
    public void playAutonomously() {
        runExperiments();
        runPostGame();
    }

}