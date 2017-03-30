package be.kuleuven.cs.gametheory.standalone;

import be.kuleuven.cs.gridflex.domain.util.MathUtils;
import be.kuleuven.cs.gametheory.AbstractGame;
import be.kuleuven.cs.gametheory.AgentGenerator;
import be.kuleuven.cs.gametheory.GameConfigurator;
import be.kuleuven.cs.gametheory.GameInstance;
import be.kuleuven.cs.gametheory.GameInstanceGenerator;
import be.kuleuven.cs.gametheory.results.GameResult;
import be.kuleuven.cs.gametheory.evolutionary.EvolutionaryGameDynamics;
import org.eclipse.jdt.annotation.NonNull;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A representation of a full game specification for all configurations of
 * agents over the action space.
 *
 * @param <N> The type of agents.
 * @param <K> The type of actions.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class Game<N, K>
        extends AbstractGame<GameInstance<N, K>, GameInstance<N, K>, List<Double>> {
    private final int agents;
    private final int actions;
    private final AgentGenerator<N> agentGen;
    private final GameInstanceGenerator<N, K> instanceGen;
    private final int reps;

    /**
     * Default constructor.
     *
     * @param agents The number of agents.
     * @param config The configurator instance.
     * @param reps   The number of repetitions.
     */
    public Game(final int agents, final GameConfigurator<N, K> config, final int reps) {
        super(agents, config.getActionSpaceSize(), reps);
        this.agents = agents;
        this.actions = config.getActionSpaceSize();
        this.agentGen = config;
        this.instanceGen = config;
        this.reps = reps;
    }

    /**
     * Configure and set-up the game specifics for different iterations of games
     * over the strategy space.
     */
    @Override
    protected void configureInstances() { //TODO Refactor method to not use actionSize before loop
        int progressCounter = 0;
        for (int iteration = 0; iteration < reps; iteration++) {
            final long combinations = MathUtils.multiCombinationSize(actions, agents);
            for (int i = 0; i < combinations; i++) {
                progressCounter++;
                printConfigProgress(progressCounter, reps * combinations);
                final GameInstance<N, K> instance = this.instanceGen
                        .generateInstance();
                final List<K> actionSet = instance.getActionSet();
                final ICombinatoricsVector<K> initialVector = Factory
                        .createVector(actionSet);
                final Generator<K> gen = Factory
                        .createMultiCombinationGenerator(initialVector, agents);

                final List<ICombinatoricsVector<K>> possCombinatoricsVectors = gen
                        .generateAllObjects();
                assert possCombinatoricsVectors.size() == combinations;
                for (final K k : possCombinatoricsVectors.get(i)) {
                    instance.fixActionToAgent(agentGen.getAgent(), k);
                }
                instance.init();
                getInternalInstanceList().add(instance);
            }
        }
    }

    @Override
    protected void runExperiments() {
        int progressCounter = 0;
        for (final GameInstance<N, K> instance : getGameInstances()) {
            progressCounter++;
            printExecutionProgress(progressCounter, getGameInstances().size());
            instance.play();
        }
    }

    @Override
    public void gatherResults(List<GameInstance<N, K>> gameInstances) {
        for (final GameInstance<N, K> instance : getGameInstances()) {
            final List<K> actionSet = instance.getActionSet();
            assert actionSet.size() == this.actions;
            final Map<N, Double> payoffResults = instance.getPayOffs();
            final Map<N, K> mapping = instance.getAgentToActionMapping();
            final int[] entry = new int[actionSet.size()];
            final Double[] values = new Double[payoffResults.keySet().size()];
            int j = 0;
            for (final Entry<N, Double> e : payoffResults.entrySet()) {
                final int whichAction = getIndexFor(actionSet,
                        mapping.get(e.getKey()));
                entry[whichAction] = entry[whichAction] + 1;
                values[j] = e.getValue();
                j++;
            }
            addPayoffEntry(values, entry);
        }
    }

    @Override
    protected GameResult<List<Double>> getResults() {
        final GameResult result = GameResult
                .create(EvolutionaryGameDynamics.from(getPayoffs()).getDynamicEquationFactors())
                .withDescription("Reps", String.valueOf(reps))
                .withDescription("agents", String.valueOf(agents))
                .withDescription("actions", String.valueOf(actions));
        return result;
    }

    private int getIndexFor(final List<@NonNull K> set, final K element) {
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i).equals(element)) {
                return i;
            }
        }
        throw new IllegalStateException("Element not found");
    }
}
