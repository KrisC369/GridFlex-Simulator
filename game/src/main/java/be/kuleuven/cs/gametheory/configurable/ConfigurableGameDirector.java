package be.kuleuven.cs.gametheory.configurable;

import be.kuleuven.cs.gametheory.AbstractGameDirector;
import be.kuleuven.cs.gametheory.results.HeuristicSymmetricPayoffMatrix;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;

/**
 * A game director for configurable games. This director generates configurations that can be
 * used for instantiation later as opposed to the direction of generating runnable instances.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class ConfigurableGameDirector
        extends AbstractGameDirector<GameInstanceConfiguration, GameInstanceResult,
        HeuristicSymmetricPayoffMatrix> {

    private final List<GameInstanceResult> results;

    /**
     * Default constructor.
     *
     * @param game The game to direct.
     */
    public ConfigurableGameDirector(ConfigurableGame game) {
        super(game);
        this.results = Lists.newArrayList();
    }

    /**
     * Call this method when an instance of the game has been played. This
     * method will perform post game-play clean-ups and hooks.
     *
     * @param finished The finished game variation.
     */
    public void notifyVersionHasBeenPlayed(GameInstanceResult gir) {
        final boolean successful = crossoff(gir);
        if (!successful) {
            throw new IllegalArgumentException(
                    "The played instance does not occur in the current game.");
        }
        this.results.add(gir);
        if (getInternalPlayables().isEmpty()) {
            runPostGame(results);
        }
    }

    private void runPostGame(List<GameInstanceResult> results) {
        getGame().gatherResults(results);
        logResults();
    }

    private boolean crossoff(GameInstanceResult gir) {
        for (GameInstanceConfiguration gi : Sets.newConcurrentHashSet(getInternalPlayables())) {
            if (gi.getAgentActionMap().equals(gir.getGameInstanceConfig().getAgentActionMap())) {
                return getInternalPlayables().remove(gi);
            }
        }
        return false;
    }
}
