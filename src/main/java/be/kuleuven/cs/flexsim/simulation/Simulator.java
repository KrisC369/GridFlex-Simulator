package be.kuleuven.cs.flexsim.simulation;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.time.SimulationClock;
import be.kuleuven.cs.flexsim.time.VirtualClock;
import be.kuleuven.cs.gridlock.simulation.events.Event;
import be.kuleuven.cs.gridlock.simulation.events.EventFactory;
import be.kuleuven.cs.gridlock.simulation.events.EventFactoryImplementation;

import com.google.common.collect.Sets;
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
    private final int duration;

    /** The internal clock reference. */
    private final SimulationClock clock;

    /** The collection of simulation components. */
    private final Set<SimulationComponent> components;

    private final EventBus eventbus;

    private final SimpleEventFactory eventFac;

    private final Set<InstrumentationComponent> instruComps;

    private final RandomGenerator random;

    private final UIDGenerator uidgen;

    private final Logger logger;

    /**
     * Instantiates a new simulator.
     * 
     * @param duration
     *            the duration
     */
    private Simulator(int duration) {
        checkArgument(duration > 0, "Duration should be strictly positive.");
        this.duration = duration;
        this.clock = new SimulationClock();
        this.components = Sets.newLinkedHashSet();
        this.instruComps = Sets.newLinkedHashSet();
        this.logger = LoggerFactory.getLogger(Simulator.class);
        this.eventbus = new EventBus("SimBus" + System.currentTimeMillis());
        this.eventFac = new SimpleEventFactory() {
            private final EventFactory ef = new EventFactoryImplementation();

            @Override
            public Event build(String eventType) {
                return ef.build(eventType, null);
            }
        };
        this.random = new MersenneTwister(duration);
        this.uidgen = new UIDGenerator() {
            private long count = 0;

            @Override
            public synchronized long getNextUID() {
                return count++;
            }
        };
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
    public int getDuration() {
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
    public VirtualClock getSimulationClock() {
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
        logger.info("Simulation started");
    }

    private void notifyStop() {
        Event ev = eventFac.build(SIMSTOP_LITERAL);
        ev.setAttribute(TIMECOUNT_LITERAL, getClock().getTimeCount());
        this.eventbus.post(ev);
        logger.info("Simulation stopped");
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
            showProgressBar();
            tickComponents();
            afterTickComponents();
        }
    }

    private void showProgressBar() {
        if (getClock().getTimeCount() * 10 % (getDuration()) == 0) {
            int perc = getClock().getTimeCount() * 100 / (getDuration());
            printProgBar(perc);
        }
    }

    private void printProgBar(int percent) {
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 50; i++) {
            if (i < (percent / 2)) {
                bar.append("=");
            } else if (i == (percent / 2)) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]   " + percent + "%     ");
        logger.info("\r" + bar.toString());
    }

    /**
     * Creates and instantiates a new simulator.
     * 
     * @param duration
     *            the duration the simulator should run for.
     * @return A new simulator object.
     */
    public static Simulator createSimulator(int duration) {
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
        logRegisterSC(comp);
        registerInstru(comp);
    }

    private void logRegisterSC(SimulationComponent comp) {
        logger.debug("Simulation component registered: {}", comp);

    }

    @Override
    public void register(InstrumentationComponent comp) {
        registerInstru(comp);
    }

    private void registerInstru(InstrumentationComponent comp) {
        this.instruComps.add(comp);
        this.eventbus.register(comp);
        comp.initialize(this);
        logRegisterIC(comp);
    }

    private void logRegisterIC(InstrumentationComponent comp) {
        logger.debug("Instrumentation component registered: {}", comp);
    }

    @Override
    public RandomGenerator getRandom() {
        return this.random;
    }

    @Override
    public UIDGenerator getUIDGenerator() {
        return this.uidgen;
    }
}
