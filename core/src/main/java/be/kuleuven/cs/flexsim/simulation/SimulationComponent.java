package be.kuleuven.cs.flexsim.simulation;

import java.util.List;

/**
 * Simulation components that need to receive tick-triggers implement this
 * interface. This interface inherits from the instrumentation component
 * interface which allows dependency injection.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
public interface SimulationComponent extends InstrumentationComponent {
    /**
     * This tick method is called after all regular ticks have been performed.
     * 
     * @param t
     *            The time step count.
     */
    void afterTick(int t);

    /**
     * The main simulation tick method. Every simulated timestep this method is
     * called.
     * 
     * @param t
     *            The time step count.
     */
    void tick(int t);

    /**
     * Get the subcomponents that are also simulation components.
     * 
     * @return the subcomponents for this component or an empty list.
     */
    List<? extends SimulationComponent> getSimulationSubComponents();
}
