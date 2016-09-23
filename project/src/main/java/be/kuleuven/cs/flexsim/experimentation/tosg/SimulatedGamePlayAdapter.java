package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.flexsim.simulation.Simulator;

import java.util.Collections;
import java.util.List;

/**
 * Adapter to trigger solving of whole horizon flex in one go using the simulator mechanism.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class SimulatedGamePlayAdapter {
    private final Simulator s;
    private final List<FlexibilityUtiliser> actions;

    /**
     * Default constructor.
     *
     * @param actions The actions to adapt.
     */
    public SimulatedGamePlayAdapter(List<FlexibilityUtiliser> actions) {

        this.s = Simulator.createSimulator(1);
        this.actions = Collections.unmodifiableList(actions);
    }

    /**
     * Play the game. Triggers the simulation.start() method.
     */
    public void play() {
        actions.forEach(agent -> s.register(new SimAdapter(() -> agent.solve())));
        s.start();
    }

    private class SimAdapter implements SimulationComponent {

        private final Runnable target;

        SimAdapter(Runnable targetFuncion) {
            this.target = targetFuncion;
        }

        @Override
        public void afterTick(int t) {
        }

        @Override
        public void tick(int t) {
            target.run();
        }

        @Override
        public List<? extends SimulationComponent> getSimulationSubComponents() {
            return Collections.emptyList();
        }

        @Override
        public void initialize(SimulationContext context) {
        }
    }
}
