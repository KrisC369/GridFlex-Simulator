package be.kuleuven.cs.gametheory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class JPPFGameDirector<N, K> extends GameDirector {

    private final Set<AbstractGameInstance<N, K>> playables;
    private final List<GameInstanceResult> results;

    /**
     * Default constructor.
     *
     * @param game The game to direct.
     */
    public JPPFGameDirector(Game<N, K> game) {
        super(game);
        this.playables = Sets.newConcurrentHashSet();
        for (GameInstance gi : game.getGameInstances()) {
            this.playables.add((AbstractGameInstance) gi);
        }
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
        if (playables.isEmpty()) {
            runPostGame(results);
        }
    }

    private void runPostGame(List<GameInstanceResult> results) {
        getGame().gatherResults(results);
        getGame().logResults();
    }

    private boolean crossoff(GameInstanceResult gir) {
        for (AbstractGameInstance gi : Sets.newConcurrentHashSet(playables)) {
            if (gi.getConfig().getAgentActionMap()
                    .equals(gir.getGameInstanceConfig().getAgentActionMap())) {
                return playables.remove(gi);
            }
        }
        return false;
    }

    /**
     * Return all the playable versions of this game.
     *
     * @return the playable variations.
     */
    public List<GameInstanceParams> getPlayableAbstractInstanceVersions() {
        return Collections.unmodifiableList(Lists.newArrayList(this.playables));
    }
}
