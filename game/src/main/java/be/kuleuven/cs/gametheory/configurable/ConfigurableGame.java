package be.kuleuven.cs.gametheory.configurable;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;
import be.kuleuven.cs.gametheory.AbstractGame;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A representation of a full game specification for all configurations of
 * agents over the action space.
 *
 * @param <N> The type of agents.
 * @param <K> The type of actions.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ConfigurableGame
        extends AbstractGame<GameInstanceConfiguration, GameInstanceResult> {
    private final int agents;
    private final int actions;
    private final int reps;

    /**
     * Default constructor.
     *
     * @param agents The number of agents.
     * @param config The configurator instance.
     * @param reps   The number of repetitions.
     */
    public ConfigurableGame(final int agents, final int actions, final int reps) {
        super(agents, actions, reps);
        this.agents = agents;
        this.actions = actions;
        this.reps = reps;
    }

    /**
     * Configure and set-up the game specifics for different iterations of games
     * over the strategy space.
     */
    protected void configureInstances() { //TODO Refactor method to not use actionSize before loop
        int progressCounter = 0;
        for (int iteration = 0; iteration < reps; iteration++) {
            final long combinations = MathUtils.multiCombinationSize(actions, agents);
            for (int i = 0; i < combinations; i++) {
                progressCounter++;
                printConfigProgress(progressCounter, reps * combinations);
                final ICombinatoricsVector<Integer> initialVector = Factory.createVector(
                        IntStream.range(0, actions).boxed().collect(Collectors.toList()));
                final Generator<Integer> gen = Factory
                        .createMultiCombinationGenerator(initialVector, agents);
                final List<ICombinatoricsVector<Integer>> possCombinatoricsVectors = gen
                        .generateAllObjects();
                assert possCombinatoricsVectors.size() == combinations;
                GameInstanceConfiguration.Builder builder = GameInstanceConfiguration.builder()
                        .setAgentSize(agents).setActionSize(actions)
                        .setSeed(GameInstanceConfiguration.DEF_SEED + iteration);
                int ag = 0;
                for (final Integer k : possCombinatoricsVectors.get(i)) {
                    builder.fixAgentToAction(ag++, k);
                }
                getInternalInstanceList().add(builder.build());
            }
        }
    }

    @Override
    protected void runExperiments() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void gatherResults(List<GameInstanceResult> instances) {
        for (final GameInstanceResult instance : instances) {
            final int[] entry = new int[actions];
            final Double[] values = new Double[agents];
            int j = 0;
            for (final Entry<Integer, Double> e : instance.getPayoffs().entrySet()) {
                final int whichAction = instance.getGameInstanceConfig().getAgentActionMap()
                        .get(e.getKey());
                entry[whichAction] = entry[whichAction] + 1;
                values[j] = e.getValue();
                j++;
            }
            addPayoffEntry(values, entry);
        }
    }
}
