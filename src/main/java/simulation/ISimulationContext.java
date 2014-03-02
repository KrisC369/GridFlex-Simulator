package simulation;

/**
 * The interface for the simulation context.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface ISimulationContext {

    /**
     * This method registers a simulationcomponent to the simulation context in
     * order to receive ticks in every simulated timestep. After registration, a
     * callback is initiated for dependency injection of this context.
     * 
     * @param comp
     *            the component to register.
     */
    abstract void register(ISimulationComponent comp);

}
