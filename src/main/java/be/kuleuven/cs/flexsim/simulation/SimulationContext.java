package be.kuleuven.cs.flexsim.simulation;

import org.apache.commons.math3.random.RandomGenerator;

import be.kuleuven.cs.flexsim.domain.util.SimpleEventFactory;
import be.kuleuven.cs.flexsim.time.VirtualClock;

import com.google.common.eventbus.EventBus;

/**
 * The interface for the simulation context.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
public interface SimulationContext {

    /**
     * Returns the eventbus for this simulation context.
     * 
     * @return the eventbus.
     */
    EventBus getEventbus();

    /**
     * Returns the eventFactory instance used for this simulation context.
     * 
     * @return an eventFactory.
     */
    SimpleEventFactory getEventFactory();

    /**
     * Return the simulation time keeper instance.
     * 
     * @return the clock
     */
    VirtualClock getSimulationClock();

    /**
     * This method registers a simulation component to the simulation context in
     * order to receive ticks in every simulated time step. After registration,
     * a callback is initiated for dependency injection of this context.
     * 
     * Besides dependency injection, simulation components receive ticks from
     * the simulator at each time step. Registration also incorporates
     * registering the component to the <code>EventBus</code>
     * 
     * @param comp
     *            the component to register.
     */
    void register(SimulationComponent comp);

    /**
     * This method registers an instrumentation component to allow for the
     * injection of the context dependency. Registration also encorporates
     * registering the component to the <code>EventBus</code>
     * 
     * @param comp
     *            the component to register.
     */
    void register(InstrumentationComponent comp);

    /**
     * This methods returns an instance of a deterministic random generator.
     * (eg. Mersenne Twister).
     * 
     * @return The Random Generator.
     */
    RandomGenerator getRandom();

}