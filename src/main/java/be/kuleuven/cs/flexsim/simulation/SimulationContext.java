package be.kuleuven.cs.flexsim.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.SimpleEventFactory;

import com.google.common.eventbus.EventBus;

/**
 * The context class for setting up simulations.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class SimulationContext {

    /** The internal clock reference. */
    /** The collection of simulation components. */
    private final List<SimulationComponent> components;
    private final EventBus eventbus;
    private final SimpleEventFactory eventFac;
    private final List<InstrumentationComponent> instruComps;

    private SimulationContext() {
        this.components = new ArrayList<>();
        this.instruComps = new ArrayList<>();
        this.eventbus = new EventBus("SimBus" + System.currentTimeMillis());
        this.eventFac = new SimpleEventFactory();
    }

    /**
     * Gets the simulation components.
     * 
     * @return the tick-receiving components of this simulation
     */
    public Collection<SimulationComponent> getSimulationComponents() {
        return Collections.unmodifiableCollection(components);
    }

    /**
     * Returns the instrumentation components of this simulator.
     * 
     * @return the instrumentation components.
     */
    public Collection<InstrumentationComponent> getInstrumentationComponents() {
        return Collections.unmodifiableCollection(instruComps);
    }

    /**
     * Returns the eventbus for this simulation context.
     * 
     * @return the eventbus.
     */
    public EventBus getEventbus() {
        return eventbus;
    }

    /**
     * Returns the eventFactory instance used for this simulation context.
     * 
     * @return an eventFactory.
     */
    public SimpleEventFactory getEventFactory() {
        return eventFac;
    }

    /**
     * This method registers a simulation component to the simulation context in
     * order to receive ticks in every simulated timestep. After registration, a
     * callback is initiated for dependency injection of this context.
     * 
     * Besides dependency injecion, simulation components recieve ticks from the
     * simulator at each timestep. Registration also encorporates registering
     * the component to the <code>EventBus</code>
     * 
     * @param comp
     *            the component to register.
     */
    public void register(SimulationComponent comp) {
        registerComp(comp);
        for (SimulationComponent sc : comp.getSimulationSubComponents()) {
            register(sc);
        }
    }

    private void registerComp(SimulationComponent comp) {
        this.components.add(comp);
        registerInstru(comp);
    }

    /**
     * This method registers an instrumentation component to allow for the
     * injection of the context dependency. Registration also encorporates
     * registering the component to the <code>EventBus</code>
     * 
     * @param comp
     *            the component to register.
     */
    public void register(InstrumentationComponent comp) {
        registerInstru(comp);
    }

    private void registerInstru(InstrumentationComponent comp) {
        this.instruComps.add(comp);
        this.eventbus.register(comp);
        comp.initialize(this);
    }

    /**
     * Create a new simulation context object.
     * 
     * @return a Simulation context object.
     */
    public static SimulationContext createDefaultContext() {
        return new SimulationContext();
    }
}