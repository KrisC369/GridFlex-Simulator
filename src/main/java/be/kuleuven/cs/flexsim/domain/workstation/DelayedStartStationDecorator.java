package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * This decorator adds the functionality of delaying the start of operation
 * (recieving ticks) by a couple of timesteps.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DelayedStartStationDecorator implements Workstation {

    private final Workstation w;
    private int shiftTime;
    private int shiftTimeAfter;

    /**
     * Default constructor for creating this decorator.
     * 
     * @param shift
     *            The amount of time steps to wait.
     * @param workstationImpl
     *            The time steps to delay execution.
     * 
     */
    DelayedStartStationDecorator(int shift, Workstation workstationImpl) {
        this.w = workstationImpl;
        this.shiftTime = shift;
        this.shiftTimeAfter = shift;
    }

    @Override
    public void afterTick() {
        if (shiftTimeAfter > 0) {
            shiftTimeAfter--;
        } else {
            w.afterTick();
        }
    }

    @Override
    public void tick() {
        if (shiftTime > 0) {
            shiftTime--;
        } else {
            w.tick();
        }
    }

    @Override
    public void initialize(SimulationContext context) {
        w.initialize(context);
    }

    @Override
    public int getLastStepConsumption() {
        return w.getLastStepConsumption();
    }

    @Override
    public int getProcessedItemsCount() {
        return w.getProcessedItemsCount();
    }

    @Override
    public int getTotalConsumption() {
        return w.getTotalConsumption();
    }

    @Override
    public boolean isIdle() {
        return w.isIdle();
    }
}
