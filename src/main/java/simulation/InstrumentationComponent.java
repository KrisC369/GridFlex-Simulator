package simulation;

/**
 * Instrumentation components allow the injection of simulation context
 * instances.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface InstrumentationComponent {

    /**
     * Method for injecting the simulationcontext dependency if needed. This
     * method is called as a callback after registering with the simulation
     * context.
     * 
     * @param context
     *            The context to inject.
     */
    void initialize(SimulationContext context);
}
