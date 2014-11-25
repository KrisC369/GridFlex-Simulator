package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;

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
    private final ActionGenerator<K> actionGen;
    private final AgentGenerator<N> agentGen;
    private final HeuristicSymmetricPayoffMatrix payoffs;
    private final GameInstanceGenerator<N, K> instanceGen;

    /**
     * Default constructor.
     * 
     * @param agents
     *            The number of agents.
     * @param agentGen
     *            The generator for agents.
     * @param action
     *            The number of actions.
     * @param actionGen
     *            The generator for the actions.
     */
    public Game(int agents, AgentGenerator<N> agentGen, int action,
            ActionGenerator<K> actionGen,
            GameInstanceGenerator<N, K> instanceGen) {
        this.agents = agents;
        this.actionGen = actionGen;
        this.actions = agents;
        this.agentGen = agentGen;
        this.payoffs = new HeuristicSymmetricPayoffMatrix(this.agents,
                this.actions);
        this.instanceGen = instanceGen;
    }

    void fillMatrix() {
        long combinations = MathUtils.multiCombinationSize(actions, agents);
        for (int i = 0; i < combinations; i++) {
            GameInstance<N, K> instance = this.instanceGen.generateInstance();
            instance.init();
            instance.fixActionToAgent(agentGen.getElement().getConcreteAgent(),
                    actionGen.getElement().getTarget());
            instance.start();
            instance.getPayOffs();
        }
    }
}
