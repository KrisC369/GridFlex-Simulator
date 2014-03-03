package simulation;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import time.Clock;

/**
 * The Class Simulator.
 */
public class Simulator implements ISimulationContext {

    /** The scheduled duration of this simulator's run. */
    private final long duration;

    /** The internal clock reference. */
    private final Clock clock;

    /** The collection of simulation components. */
    private final List<ISimulationComponent> components;

    /**
     * Instantiates a new simulator.
     * 
     * @param duration
     *            the duration
     */
    private Simulator(long duration) {
        checkArgument(duration > 0, "Duration should be strictly positive.");
        this.duration = duration;
        this.clock = new Clock();
        this.components = new ArrayList<ISimulationComponent>();
    }

    /**
     * Starts this simulation by running the simulation loop.
     * 
     */
    public void start() {
        simloop();
    }

    private void simloop() {
        while (shouldRun()) {
            getClock().addTimeStep(1);
            tickComponents();
        }
    }

    private synchronized void tickComponents() {
        for (ISimulationComponent c : components) {
            c.tick();
        }
    }

    private boolean shouldRun() {
        if (getClock().getTimeCount() >= getDuration()) { return false; }
        return true;

    }

    private Clock getClock() {
        return this.clock;
    }

    /**
     * Gets the scheduled duration of this simulation.
     * 
     * @return the duration
     */
    public long getDuration() {
        return this.duration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * simulation.ISimulationContext#register(simulation.ISimulationComponent)
     */
    @Override
    public void register(ISimulationComponent comp) {
        this.components.add(comp);

    }

    /**
     * Gets the simulation components.
     * 
     * @return the components of this simulation
     */
    public Collection<ISimulationComponent> getComponents() {
        return Collections.unmodifiableCollection(components);
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
     *  Creates and instantiates a new simulator.
     * @param duration the duration the simulator should run for.
     * @return A new simulator object.
     */
    public static Simulator createSimulator(long duration){
        return new Simulator(duration);
    }

}
