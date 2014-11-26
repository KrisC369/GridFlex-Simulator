package be.kuleuven.cs.gametheory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

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
    private final int agents;
    private final int actions;
    // private final ActionGenerator<K> actionGen;
    private final AgentGenerator<N> agentGen;
    private final HeuristicSymmetricPayoffMatrix payoffs;
    private final GameInstanceGenerator<N, K> instanceGen;
    private final int reps;

    /**
     * Default constructor.
     * 
     * @param agents
     *            The number of agents.
     * @param agentGen
     *            The generator for agents.
     * @param action
     *            The number of actions.
     * @param instanceGen
     *            The generator for game instances.
     */
    public Game(int agents, AgentGenerator<N> agentGen, int actions,
    // ActionGenerator<K> actionGen,
            GameInstanceGenerator<N, K> instanceGen, int reps) {
        this.agents = agents;
        // this.actionGen = actionGen;
        this.actions = actions;
        this.agentGen = agentGen;
        this.payoffs = new HeuristicSymmetricPayoffMatrix(this.agents,
                this.actions);
        this.instanceGen = instanceGen;
        this.reps = reps;
    }

    void fillMatrix() {
        for (int iterations = 0; iterations < reps; iterations++) {
            long combinations = MathUtils.multiCombinationSize(actions, agents);
            for (int i = 0; i < combinations; i++) {
                GameInstance<N, K> instance = this.instanceGen
                        .generateInstance();
                List<K> actionSet = instance.getActionSet();
                ICombinatoricsVector<K> initialVector = Factory
                        .createVector(actionSet);
                Generator<K> gen = Factory.createMultiCombinationGenerator(
                        initialVector, agents);

                List<ICombinatoricsVector<K>> possCombinatoricsVectors = gen
                        .generateAllObjects();
                assert (possCombinatoricsVectors.size() == combinations);
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
}
