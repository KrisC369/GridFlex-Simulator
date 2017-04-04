package be.kuleuven.cs.gridflex.simulation;

import java.util.Collections;
import java.util.List;

/**
 * Simulation components that need to receive tick-triggers implement this
 * interface. This interface inherits from the instrumentation component
 * interface which allows dependency injection.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface SimulationComponent extends InstrumentationComponent {
    /**
     * This tick method is called after all regular ticks have been performed.
     * The default implementation is to do nothing.
     *
     * @param t The time step count.
     */
    default void afterTick(int t) {
    }

    /**
     * The main simulation tick method. Every simulated timestep this method is
     * called.
     *
     * @param t The time step count.
     */
    void tick(int t);

    /**
     * Get the subcomponents that are also simulation components.
     * Default implementation is to return an empty list.
     *
     * @return the subcomponents for this component or an empty list.
     */
    default List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }
}
