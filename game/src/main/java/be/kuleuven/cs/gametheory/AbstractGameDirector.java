package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.gridflex.io.Writable;
import be.kuleuven.cs.gametheory.results.GameResult;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * This instance governs the sequential rules and protocols involved when
 * setting up and playing games.
 *
 * @param <T> The type instances for games returned.
 * @param <I> The type of results expected to be handled.
 * @param <R> The main result type to aggregate and return.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class AbstractGameDirector<T extends Playable, I, R> {

    private final AbstractGame<T, I, R> game;
    private final List<T> playables;

    /**
     * Default constructor.
     *
     * @param game The game to direct.
     */
    public AbstractGameDirector(final AbstractGame<T, I, R> game) {
        this.game = game;
        this.game.configureInstances();
        this.playables = Lists.newArrayList();
        this.playables.addAll(game.getGameInstances());
    }

    /**
     * Return all the playable versions of this game.
     *
     * @return the playable variations.
     */
    public List<T> getPlayableVersions() {
        return Collections.unmodifiableList(Lists.newArrayList(this.playables));
    }

    /**
     * @return The internal list, backed.
     */
    protected List<T> getInternalPlayables() {
        return this.playables;
    }

    protected void logResults() {
        getGame().logResults();
    }

    protected void runExperiments() {
        getGame().runExperiments();
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
    @Deprecated
    public String getDynamicEquationArguments() {
        return game.getDynamicsParametersString();
    }

    /**
     * Constructs the results from the current game.
     *
     * @return A gameresult object based on the currently available result date
     * for this game.
     */
    public GameResult<R> getResults() {
        return game.getResults();
    }
    // TODO check state (have experiments been run or not, before drawing upon
    // results).

    /**
     * @return This director's game instance.
     */
    protected AbstractGame<T, I, R> getGame() {
        return game;
    }
}

