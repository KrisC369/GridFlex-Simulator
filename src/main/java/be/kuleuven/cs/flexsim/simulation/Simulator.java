package be.kuleuven.cs.flexsim.simulation;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.SimpleEventFactory;
import be.kuleuven.cs.flexsim.time.Clock;
import be.kuleuven.cs.flexsim.time.SimulationClock;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.eventbus.EventBus;

/**
 * The Class Simulator.
 */
public class Simulator implements SimulationContext {

    /** The scheduled duration of this simulator's run. */
    private final long duration;

    /** The internal clock reference. */
    private final SimulationClock clock;

    /** The collection of simulation components. */
    private final List<SimulationComponent> components;

    private final EventBus eventbus;

    private final SimpleEventFactory eventFac;

    private List<InstrumentationComponent> instruComps;

    /**
     * Instantiates a new simulator.
     * 
     * @param duration
     *            the duration
     */
    private Simulator(long duration) {
        checkArgument(duration > 0, "Duration should be strictly positive.");
        this.duration = duration;
        this.clock = new SimulationClock();
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
     * Gets the scheduled duration of this simulation.
     * 
     * @return the duration
     */
    public long getDuration() {
        return this.duration;
    }

    @Override
    public EventBus getEventbus() {
        return eventbus;
    }

    @Override
    public SimpleEventFactory getEventFactory() {
        return eventFac;
    }

    /**
     * Gets the elapsed simulation time.
     * 
     * @return the simulation time
     */
    public int getSimulationTime() {
        return clock.getTimeCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * simulation.ISimulationContext#register(simulation.ISimulationComponent)
     */
    @Override
    public void register(SimulationComponent comp) {
        registerComp(comp);
    }

    private void registerComp(SimulationComponent comp) {
        this.components.add(comp);
        registerInstru(comp);
    }

    /**
     * Starts this simulation by running the simulation loop.
     * 
     */
    public void start() {
        notifyStart();
        simloop();
        notifyStop();
    }

    private void notifyStop() {
        Event ev = eventFac.build("simulation:stopped");
        ev.setAttribute("clocktime", getClock().getTimeCount());
        this.eventbus.post(ev);
    }

    private synchronized void afterTickComponents() {
        for (SimulationComponent c : components) {
            c.afterTick();
        }
    }

    private SimulationClock getClock() {
        return this.clock;
    }

    private void notifyStart() {
        Event ev = eventFac.build("simulation:started");
        ev.setAttribute("clocktime", getClock().getTimeCount());
        this.eventbus.post(ev);
    }

    private boolean shouldRun() {
        if (getClock().getTimeCount() >= getDuration()) {
            return false;
        }
        return true;

    }

    private void simloop() {
        while (shouldRun()) {
            getClock().addTimeStep(1);
            tickComponents();
            afterTickComponents();
        }
    }

    private synchronized void tickComponents() {
        for (SimulationComponent c : components) {
            c.tick();
        }
    }

    /**
     * Creates and instantiates a new simulator.
     * 
     * @param duration
     *            the duration the simulator should run for.
     * @return A new simulator object.
     */
    public static Simulator createSimulator(long duration) {
        return new Simulator(duration);
    }

    /**
     * Returns the instrumentation components of this simulator
     * 
     * @return the instrumentation components.
     */
    public Collection<InstrumentationComponent> getInstrumentationComponents() {
        return Collections.unmodifiableCollection(instruComps);
    }

    @Override
    public void register(InstrumentationComponent comp) {
        registerInstru(comp);
    }

    private void registerInstru(InstrumentationComponent comp) {
        this.instruComps.add(comp);
        this.eventbus.register(comp);
        comp.initialize(this);
    }

    @Override
    public Clock getSimulationClock() {
        return this.clock;
    }

}
