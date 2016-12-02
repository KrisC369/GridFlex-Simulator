package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.flexsim.io.Writable;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * This instance governs the sequential rules and protocols involved when
 * setting up and playing games.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class AbstractGameDirector<T, I> {

    private final AbstractGame<T, I> game;
    private final List<T> playables;

    /**
     * Default constructor.
     *
     * @param game The game to direct.
     */
    public AbstractGameDirector(final AbstractGame<T, I> game) {
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
    public String getDynamicEquationArguments() {
        return game.getDynamicsParametersString();
    }

    /**
     * Constructs the results from the current game.
     *
     * @return A gameresult object based on the currently available result date
     * for this game.
     */
    public GameResult getResults() {
        return game.getResults();
    }
    // TODO check state (have experiments been run or not, before drawing upon
    // results).

    /**
     * @return This director's game instance.
     */
    protected AbstractGame<T, I> getGame() {
        return game;
    }
}
