package simulation;

/**
 * Simulation components that need to receive tick-triggers implement this
 * interface.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface ISimulationComponent {
    /**
     * This tick method is called after all regular ticks have been performed.
     */
    void afterTick();

    /**
     * Method for injecting the simulationcontext dependency if needed. This
     * method is called as a callback after registering with the simulation
     * context.
     * 
     * @param context
     *            The context to inject.
     */
    void initialize(ISimulationContext context);

    /**
     * The main simulation tick method. Every simulated timestep this method is
     * called.
     */
    void tick();

}
