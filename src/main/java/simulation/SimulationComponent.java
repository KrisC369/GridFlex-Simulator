package simulation;

/**
 * Simulation components that need to receive tick-triggers implement this
 * interface. This interface inherits from the instrumentation component
 * interface which allows dependency injecion.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface SimulationComponent extends InstrumentationComponent {
    /**
     * This tick method is called after all regular ticks have been performed.
     */
    void afterTick();

    /**
     * The main simulation tick method. Every simulated timestep this method is
     * called.
     */
    void tick();

}
