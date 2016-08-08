package be.kuleuven.cs.flexsim.simulation;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import be.kuleuven.cs.flexsim.event.Event;
import be.kuleuven.cs.flexsim.event.EventFactory;
import be.kuleuven.cs.flexsim.event.EventFactoryImplementation;
import be.kuleuven.cs.flexsim.time.SimulationClock;
import be.kuleuven.cs.flexsim.time.VirtualClock;

/**
 * The Class Simulator. This Simulator drives the simulation by sending ticks to
 * signify the passing of time. This simulator implements the
 * <code>SimulationContext</code>-interface.
 */
public final class Simulator implements SimulationContext {

    private static final int PROGRESSBAR_LENGTH = 50;

    private static final int PROGRESS_SCALE_FACTOR = 10;

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

    private final EventFactory eventFac;

    private final Set<InstrumentationComponent> instruComps;

    private final RandomGenerator random;

    private final UIDGenerator uidgen;

    private final Logger logger;

    /**
     * Instantiates a new simulator.
     *
     * @param duration
     *            the duration
     * @param seed
     *            The seed.
     */
    private Simulator(final int duration, final int seed) {
        checkArgument(duration > 0, "Duration should be strictly positive.");
        this.duration = duration;
        this.clock = new SimulationClock();
        this.components = Sets.newLinkedHashSet();
        this.instruComps = Sets.newLinkedHashSet();
        this.logger = LoggerFactory.getLogger(Simulator.class);
        this.eventbus = new EventBus("SimBus" + System.currentTimeMillis());
        this.eventFac = new EventFactoryImplementation();
        this.random = new MersenneTwister(seed);
        this.uidgen = new UIDGenerator() {
            private long count = 0;

            @Override
            public synchronized long getNextUID() {
                return count++;
            }
        };
    }

    /**
     * Instantiates a new simulator.
     *
     * @param duration
     *            the duration
     */
    private Simulator(final int duration2) {
        this(duration2, duration2);
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
    public EventFactory getEventFactory() {
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
     */
    public void start() {
        notifyStart();
        simloop();
        notifyStop();
    }

    private void notifyStart() {
        final Event ev = eventFac.build(SIMSTART_LITERAL);
        ev.setAttribute(TIMECOUNT_LITERAL, getClock().getTimeCount());
        this.eventbus.post(ev);
        logger.info("Simulation started");
    }

    private void notifyStop() {
        final Event ev = eventFac.build(SIMSTOP_LITERAL);
        ev.setAttribute(TIMECOUNT_LITERAL, getClock().getTimeCount());
        this.eventbus.post(ev);
        logger.info("Simulation stopped");
    }

    private synchronized void tickComponents() {
        for (final SimulationComponent c : components) {
            c.tick(getSimulationTime());
        }
    }

    private synchronized void afterTickComponents() {
        for (final SimulationComponent c : components) {
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
        if (getClock().getTimeCount() * PROGRESS_SCALE_FACTOR
                % (getDuration()) == 0) {
            final int perc = getClock().getTimeCount() * PROGRESS_SCALE_FACTOR
                    * PROGRESS_SCALE_FACTOR / (getDuration());
            printProgBar(perc);
        }
    }

    private void printProgBar(final int percent) {
        final StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < PROGRESSBAR_LENGTH; i++) {
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
    public static Simulator createSimulator(final int duration) {
        return new Simulator(duration);
    }

    /**
     * Creates and instantiates a new simulator with a given seed for the PRNG.
     *
     * @param duration
     *            the duration the simulator should run for.
     * @param seed
     *            The seed.
     * @return A new simulator object.
     */
    public static Simulator createSimulator(final int duration, final int seed) {
        return new Simulator(duration, seed);
    }

    /*
     * (non-Javadoc)
     * @see
     * simulation.ISimulationContext#register(simulation.ISimulationComponent)
     */
    @Override
    public void register(final SimulationComponent comp) {
        registerComp(comp);
        comp.getSimulationSubComponents().forEach(this::register);
    }

    private void registerComp(final SimulationComponent comp) {
        this.components.add(comp);
        logRegisterSC(comp);
        registerInstru(comp);
    }

    private void logRegisterSC(final SimulationComponent comp) {
        logger.debug("Simulation component registered: {}", comp);

    }

    @Override
    public void register(final InstrumentationComponent comp) {
        registerInstru(comp);
    }

    private void registerInstru(final InstrumentationComponent comp) {
        this.instruComps.add(comp);
        this.eventbus.register(comp);
        comp.initialize(this);
        logRegisterIC(comp);
    }

    private void logRegisterIC(final InstrumentationComponent comp) {
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
