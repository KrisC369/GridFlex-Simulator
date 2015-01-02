package be.kuleuven.cs.gametheory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;
import be.kuleuven.cs.gametheory.experimentation.Writable;

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
public class Game<N, K> implements Writable {
    private final int agents;
    private final int actions;
    private final AgentGenerator<N> agentGen;
    private final HeuristicSymmetricPayoffMatrix payoffs;
    private final GameInstanceGenerator<N, K> instanceGen;
    private final int reps;
    private final Logger logger;

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
        this.logger = LoggerFactory.getLogger(Game.class);
    }

    private void fillMatrix() {
        int progressCounter = 0;
        for (int iteration = 0; iteration < reps; iteration++) {
            long combinations = MathUtils.multiCombinationSize(actions, agents);
            for (int i = 0; i < combinations; i++) {
                progressCounter++;
                printProgress(progressCounter, reps * combinations);
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
                instance.start();

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
    }

    private void printProgress(int progressCounter, long l) {
        StringBuilder b = new StringBuilder();
        b.append("Simulating instance: ").append(progressCounter).append("/")
                .append(l);
        logger.warn(b.toString());
    }

    private int getIndexFor(List<K> set, K element) {
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i).equals(element)) {
                return i;
            }
        }
        throw new IllegalStateException("Element not found");
    }

    /**
     * Run the experiment and print the matrix.
     */
    public void runExperiment() {
        fillMatrix();
        payoffs.printMatrix();
    }

    @Override
    public String getFormattedResultString() {
        return payoffs.toString();
    }

    /**
     * Returns the parameters of the dynamics in a formatted string.
     * 
     * @return the params in a MATLAB style formatted string.
     */
    public String getDynamicsParametersString() {
        StringBuilder b = new StringBuilder();
        char character = 'a';
        b.append("\n");
        for (Double d : payoffs.getDynamicsArguments()) {
            b.append(character++).append("=").append(d).append(";\n");
        }
        return b.toString();
    }
}
