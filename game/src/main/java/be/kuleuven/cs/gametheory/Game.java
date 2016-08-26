package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;
import com.google.common.collect.Lists;
import org.eclipse.jdt.annotation.NonNull;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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
public class Game<N, K> {
    private static final String CONSOLE = "CONSOLE";
    private static final String CONFIGURING = "Configuring instance: ";
    private static final String EXECUTING = "Executing instance: ";
    private final int agents;
    private final int actions;
    private final AgentGenerator<N> agentGen;
    private final HeuristicSymmetricPayoffMatrix payoffs;
    private final GameInstanceGenerator<N, K> instanceGen;
    private final int reps;
    private final Logger logger;
    private List<GameInstance<N, K>> instanceList;

    /**
     * Default constructor.
     *
     * @param agents The number of agents.
     * @param config The configurator instance.
     * @param reps   The number of repetitions.
     */
    public Game(final int agents, final GameConfigurator<N, K> config, final int reps) {
        this.agents = agents;
        this.actions = config.getActionSpaceSize();
        this.agentGen = config;
        this.payoffs = new HeuristicSymmetricPayoffMatrix(this.agents, this.actions);
        this.instanceGen = config;
        this.reps = reps;
        this.logger = LoggerFactory.getLogger(CONSOLE);
        this.instanceList = Lists.newArrayList();
    }

    /**
     * Configure and set-up the game specifics for different iterations of games
     * over the strategy space.
     */
    void configureInstances() { //TODO Refactor method to not use actionSize before loop
        this.instanceList = Lists.newArrayList();
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
                this.instanceList.add(instance);
            }
        }
    }

    void runExperiments() {
        int progressCounter = 0;
        for (final GameInstance<N, K> instance : instanceList) {
            progressCounter++;
            printExecutionProgress(progressCounter, instanceList.size());
            instance.play();
        }
    }

    List<GameInstance<N, K>> getGameInstances() {
        return Collections.unmodifiableList(this.instanceList);
    }

    void gatherResults() {
        for (final GameInstance<N, K> instance : instanceList) {
            final List<K> actionSet = instance.getActionSet();
            assert (actionSet.size() == this.agents);
            final Map<N, Long> payoffResults = instance.getPayOffs();
            final Map<N, K> mapping = instance.getAgentToActionMapping();
            final List<K> actualActions = Lists.newArrayList();
            for (final Entry<N, K> e : mapping.entrySet()) {
                actualActions.add(e.getValue());
            }
            final int[] entry = new int[actionSet.size()];
            final long[] values = new long[payoffResults.keySet().size()];
            int j = 0;
            for (final Entry<N, Long> e : payoffResults.entrySet()) {
                final int whichAction = getIndexFor(actionSet,
                        mapping.get(e.getKey()));
                entry[whichAction] = entry[whichAction] + 1;
                values[j] = e.getValue();
                j++;
            }
            this.payoffs.addEntry(values, entry);
        }
    }

    private void printConfigProgress(final int progressCounter, final long l) {
        printProgress(progressCounter, l, CONFIGURING);

    }

    private void printExecutionProgress(final int progressCounter, final long l) {
        printProgress(progressCounter, l, EXECUTING);
    }

    private void printProgress(final int progressCounter, final long l, final String message) {
        final StringBuilder b = new StringBuilder();
        b.append(message).append(progressCounter).append("/").append(l);
        logger.info(b.toString());
    }

    private int getIndexFor(final List<@NonNull K> set, final K element) {
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i).equals(element)) {
                return i;
            }
        }
        throw new IllegalStateException("Element not found");
    }

    void logResults() {
        final StringBuilder b = new StringBuilder(30);
        b.append(getResultString()).append("\n")
                .append("Dynamics equation params:");
        for (final Double d : payoffs.getDynamicEquationFactors()) {
            b.append(d).append("\n");
        }
        logger.info(b.toString());
    }

    String getResultString() {
        return payoffs.toString();
    }

    /**
     * Returns the parameters of the dynamics in a formatted string.
     *
     * @return the params in a MATLAB style formatted string.
     */
    String getDynamicsParametersString() {
        final StringBuilder b = new StringBuilder(30);
        char character = 'a';
        b.append("\n");
        for (final Double d : payoffs.getDynamicEquationFactors()) {
            b.append(character++).append("=").append(d).append(";\n");
        }
        return b.toString();
    }

    /**
     * Constructs the results from the current game.
     *
     * @return A gameresult object based on the currently available result date
     * for this game.
     */
    GameResult getResults() {
        final GameResult result = GameResult
                .create(payoffs.getDynamicEquationFactors())
                .withDescription("Reps", String.valueOf(reps))
                .withDescription("agents", String.valueOf(agents))
                .withDescription("actions", String.valueOf(actions));
        return result;
    }
}
