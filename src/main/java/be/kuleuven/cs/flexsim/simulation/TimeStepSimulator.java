package be.kuleuven.cs.flexsim.simulation;

import static com.google.common.base.Preconditions.checkArgument;
import be.kuleuven.cs.flexsim.time.SimulationClock;
import be.kuleuven.cs.gridlock.simulation.events.Event;

/**
 * The Class Simulator. This Simulator drives the simulation by sending ticks to
 * signify the passing of time. This simulator implements the
 * <code>SimulationContext</code>-interface.
 */
public final class TimeStepSimulator {

    private static final String SIMSTOP_LITERAL = "simulation:stopped";

    private static final String SIMSTART_LITERAL = "simulation:started";

    private static final String TIMECOUNT_LITERAL = "clocktime";

    /** The internal clock reference. */
    private SimulationClock clock;

    /** The scheduled duration of this simulator's run. */
    private final long duration;

    private final SimulationContext context;

    /**
     * Instantiates a new simulator.
     * 
     * @param duration
     *            the duration
     */
    private TimeStepSimulator(long duration, SimulationContext context) {
        checkArgument(duration > 0, "Duration should be strictly positive.");
        this.duration = duration;
        this.context = context;
        this.clock = new SimulationClock();

    }

    /**
     * Gets the scheduled duration of this simulation.
     * 
     * @return the duration
     */
    public long getDuration() {
        return this.duration;
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
        Event ev = context.getEventFactory().build(SIMSTART_LITERAL);
        ev.setAttribute(TIMECOUNT_LITERAL, getClock().getTimeCount());
        this.context.getEventbus().post(ev);
    }

    private void notifyStop() {
        Event ev = context.getEventFactory().build(SIMSTOP_LITERAL);
        ev.setAttribute(TIMECOUNT_LITERAL, getClock().getTimeCount());
        this.context.getEventbus().post(ev);
    }

    private synchronized void tickComponents() {
        for (SimulationComponent c : getContext().getSimulationComponents()) {
            c.tick(getSimulationTime());
        }
    }

    private synchronized void afterTickComponents() {
        for (SimulationComponent c : getContext().getSimulationComponents()) {
            c.afterTick(getSimulationTime());
        }
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
     *            The duration the simulator should run for.
     * @param context
     *            The simulation context.
     * @return A new simulator object.
     */
    public static TimeStepSimulator createSimulator(long duration,
            SimulationContext context) {
        return new TimeStepSimulator(duration, context);
    }

    SimulationClock getClock() {
        return this.clock;
    }

    private SimulationContext getContext() {
        return this.context;
    }

    /**
     * Gets the elapsed simulation time.
     * 
     * @return the simulation time
     */
    public int getSimulationTime() {
        return clock.getTimeCount();
    }
}
