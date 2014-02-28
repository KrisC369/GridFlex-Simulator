package simulation;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import time.Clock;

public class Simulator implements ISimulationContext {

    private long duration;
    private final Clock clock;
    private final List<ISimulationComponent> components;

    public Simulator(long duration) {
        checkArgument(duration > 0, "Duration should be strictly positive.");
        this.duration = duration;
        this.clock = new Clock();
        this.components = new ArrayList<ISimulationComponent>();
    }

    public void start(boolean immediateReturn) {
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
        if (getClock().getTimeCount() >= getDuration()) {
            return false;
        }
        return true;

    }

    private Clock getClock() {
        return this.clock;
    }

    public long getDuration() {
        return this.duration;
    }

    @Override
    public void register(ISimulationComponent comp) {
        this.components.add(comp);

    }

    public Collection<ISimulationComponent> getComponents() {
        return Collections.unmodifiableCollection(components);
    }

    public int getSimulationTime() {
        return clock.getTimeCount();
    }

}
