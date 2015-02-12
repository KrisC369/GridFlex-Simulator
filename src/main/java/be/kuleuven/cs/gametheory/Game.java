package be.kuleuven.cs.gametheory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;

import com.google.common.collect.Lists;

/**
 * A representation of a full game specification for all configurations of
 * agents over the action space.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 * @param <N>
 *            The type of agents.
 * @param <K>
 *            The type of actions.
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
     * @param agents
     *            The number of agents.
     * @param config
     *            The configurator instance.
     * @param reps
     *            The number of repetitions.
     */
    public Game(int agents, GameConfigurator<N, K> config, int reps) {
        this.agents = agents;
        this.actions = config.getActionSpaceSize();
        this.agentGen = config;
        this.payoffs = new HeuristicSymmetricPayoffMatrix(this.agents,
                this.actions);
        this.instanceGen = config;
        this.reps = reps;
        this.logger = LoggerFactory.getLogger(CONSOLE);
        this.instanceList = Lists.newArrayList();
    }

    /**
     * Configure and set-up the game specifics.
     */
    void configureInstances() {
        this.instanceList = Lists.newArrayList();
        int progressCounter = 0;
        for (int iteration = 0; iteration < reps; iteration++) {
            long combinations = MathUtils.multiCombinationSize(actions, agents);
            for (int i = 0; i < combinations; i++) {
                progressCounter++;
                printConfigProgress(progressCounter, reps * combinations);
                GameInstance<N, K> instance = this.instanceGen
                        .generateInstance();
                List<K> actionSet = instance.getActionSet();
                ICombinatoricsVector<K> initialVector = Factory
                        .createVector(actionSet);
                Generator<K> gen = Factory.createMultiCombinationGenerator(
                        initialVector, agents);

                List<ICombinatoricsVector<K>> possCombinatoricsVectors = gen
                        .generateAllObjects();
                assert possCombinatoricsVectors.size() == combinations;
                for (K k : possCombinatoricsVectors.get(i)) {
                    instance.fixActionToAgent(agentGen.getAgent(), k);
                }
                instance.init();
                this.instanceList.add(instance);
            }
        }
    }

    void runExperiments() {
        int progressCounter = 0;
        for (GameInstance<N, K> instance : instanceList) {
            progressCounter++;
            printExecutionProgress(progressCounter, instanceList.size());
            instance.play();
        }
    }

    List<GameInstance<N, K>> getGameInstances() {
        return Collections.unmodifiableList(this.instanceList);
    }

    void gatherResults() {
        for (GameInstance<N, K> instance : instanceList) {
            List<K> actionSet = instance.getActionSet();
            Map<N, Long> payoffResults = instance.getPayOffs();
            Map<N, K> mapping = instance.getAgentToActionMapping();
            List<K> actualActions = Lists.newArrayList();
            for (Entry<N, K> e : mapping.entrySet()) {
                actualActions.add(e.getValue());
            }
            int[] entry = new int[actionSet.size()];
            long[] values = new long[payoffResults.keySet().size()];
            int j = 0;
            for (Entry<N, Long> e : payoffResults.entrySet()) {
                int whichAction = getIndexFor(actionSet,
                        mapping.get(e.getKey()));
                entry[whichAction] = entry[whichAction] + 1;
                values[j] = e.getValue();
                j++;
            }
            this.payoffs.addEntry(values, entry);
        }
    }

    private void printConfigProgress(int progressCounter, long l) {
        printProgress(progressCounter, l, CONFIGURING);

    }

    private void printExecutionProgress(int progressCounter, long l) {
        printProgress(progressCounter, l, EXECUTING);
    }

    private void printProgress(int progressCounter, long l, String message) {
        StringBuilder b = new StringBuilder();
        b.append(message).append(progressCounter).append("/").append(l);
        logger.info(b.toString());
    }

    private int getIndexFor(List<K> set, K element) {
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i).equals(element)) {
                return i;
            }
        }
        throw new IllegalStateException("Element not found");
    }

    void logResults() {
        StringBuilder b = new StringBuilder();
        b.append(getResultString()).append("\n")
                .append("Dynamics equation params:");
        for (Double d : payoffs.getDynamicEquationFactors()) {
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
        StringBuilder b = new StringBuilder();
        char character = 'a';
        b.append("\n");
        for (Double d : payoffs.getDynamicEquationFactors()) {
            b.append(character++).append("=").append(d).append(";\n");
        }
        return b.toString();
    }
}
