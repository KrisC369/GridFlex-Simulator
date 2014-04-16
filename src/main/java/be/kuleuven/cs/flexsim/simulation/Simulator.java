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
 * The Class Simulator. This Simulator drives the simulation by sending ticks to
 * signify the passing of time. This simulator implements the
 * <code>SimulationContext</code>-interface.
 */
public final class Simulator implements SimulationContext {

    private static final String SIMSTOP_LITERAL = "simulation:stopped";

    private static final String SIMSTART_LITERAL = "simulation:started";

    private static final String TIMECOUNT_LITERAL = "clocktime";

    /** The scheduled duration of this simulator's run. */
    private final long duration;

    /** The internal clock reference. */
    private final SimulationClock clock;

    /** The collection of simulation components. */
    private final List<SimulationComponent> components;

    private final EventBus eventbus;

    private final SimpleEventFactory eventFac;

    private final List<InstrumentationComponent> instruComps;

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
     * Returns the instrumentation components of this simulator.
     * 
     * @return the instrumentation components.
     */
    public Collection<InstrumentationComponent> getInstrumentationComponents() {
        return Collections.unmodifiableCollection(instruComps);
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

    @Override
    public Clock getSimulationClock() {
        return this.clock;
    }

    /**
     * Gets the elapsed simulation time.
     * 
     * @return the simulation time
     */
    public int getSimulationTime() {
        return clock.getTimeCount();
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

    private void notifyStart() {
        Event ev = eventFac.build(SIMSTART_LITERAL);
        ev.setAttribute(TIMECOUNT_LITERAL, getClock().getTimeCount());
        this.eventbus.post(ev);
    }

    private void notifyStop() {
        Event ev = eventFac.build(SIMSTOP_LITERAL);
        ev.setAttribute(TIMECOUNT_LITERAL, getClock().getTimeCount());
        this.eventbus.post(ev);
    }

    private synchronized void tickComponents() {
        for (SimulationComponent c : components) {
            c.tick(getSimulationTime());
        }
    }

    private synchronized void afterTickComponents() {
        for (SimulationComponent c : components) {
            c.afterTick(getSimulationTime());
        }
    }

    private SimulationClock getClock() {
        return this.clock;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * simulation.ISimulationContext#register(simulation.ISimulationComponent)
     */
    @Override
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

    @Override
    public void register(InstrumentationComponent comp) {
        registerInstru(comp);
    }

    private void registerInstru(InstrumentationComponent comp) {
        this.instruComps.add(comp);
        this.eventbus.register(comp);
        comp.initialize(this);
    }

}
