package be.kuleuven.cs.gametheory;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import be.kuleuven.cs.gametheory.io.Writable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * This instance governes the sequantial rules and protocols involved when
 * setting up and playing games.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class GameDirector implements Serializable {

    private static final long serialVersionUID = 3521800307605408441L;
    private final Game<?, ?> game;
    private Set<Playable> playables;

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
        for (GameInstance<?, ?> i : game.getGameInstances()) {
            this.playables.add(i);
        }
        return Collections.unmodifiableList(Lists.newArrayList(this.playables));
    }

    /**
     * Return the results in a formatted string.
     *
     * @return the results in string format.
     */
    public Writable getFormattedResults() {
        return new Writable() {
            @Override
            public String getFormattedResultString() {
                return game.getResultString();
            }
        };
    }

    /**
     * Return the results in a formatted string.
     *
     * @return the results in string format.
     */
    public String getDynamicEquationArguments() {
        return game.getDynamicsParametersString();
    }
}
