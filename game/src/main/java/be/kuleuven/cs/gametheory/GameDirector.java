package be.kuleuven.cs.gametheory;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import be.kuleuven.cs.flexsim.io.Writable;

/**
 * This instance governes the sequantial rules and protocols involved when
 * setting up and playing games.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class GameDirector {

    private final Game<?, ?> game;
    private final Set<Playable> playables;

    /**
     * Default constructor.
     *
     * @param game
     *            The game to direct.
     */
    public GameDirector(Game<?, ?> game) {
        this.game = game;
        game.configureInstances();
        this.playables = Sets.newConcurrentHashSet();
    }

    /**
     * Call this method when an instance of the game has been played. This
     * method will perform post game-play clean-ups and hooks.
     *
     * @param finished
     *            The finished game variation.
     */
    public void notifyVersionHasBeenPlayed(Playable finished) {
        boolean succesfull = playables.remove(finished);
        if (!succesfull) {
            throw new IllegalArgumentException(
                    "The played instance does not occur in the current game.");
        }
        if (playables.isEmpty()) {
            game.gatherResults();
            game.logResults();
        }
    }

    /**
     * Play a full game specification autonomously.
     */
    public void playAutonomously() {
        game.runExperiments();
        game.gatherResults();
        game.logResults();
    }

    /**
     * Return all the playable versions of this game.
     *
     * @return the playable variations.
     */
    public List<Playable> getPlayableVersions() {
        this.playables.addAll(game.getGameInstances());
        return Collections.unmodifiableList(Lists.newArrayList(this.playables));
    }

    /**
     * Return the results in a formatted string.
     *
     * @return the results in string format.
     */
    public Writable getFormattedResults() {
        return () -> game.getResultString();
    }

    /**
     * Return the results in a formatted string.
     *
     * @return the results in string format.
     */
    public String getDynamicEquationArguments() {
        return game.getDynamicsParametersString();
    }

    /**
     * Constructs the results from the current game.
     *
     * @return A gameresult object based on the currently available result date
     *         for this game.
     */
    public GameResult getResults() {
        return game.getResults();
    }
    // TODO check state (have experiments been run or not, before drawing upon
    // results).
}
