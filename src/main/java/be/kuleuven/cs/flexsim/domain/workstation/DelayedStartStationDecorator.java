package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * This decorator adds the functionality of delaying the start of operation
 * (recieving ticks) by a couple of timesteps.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DelayedStartStationDecorator extends ForwardingStationDecorator {

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
        super(workstationImpl);
        this.shiftTime = shift;
        this.shiftTimeAfter = shift;
    }

    /**
     * {@inheritDoc}
     * This tick method is called after a number of initial ticks.
     */
    @Override
    public void afterTick() {
        if (shiftTimeAfter > 0) {
            shiftTimeAfter--;
        } else {
            super.afterTick();
        }
    }

    /**
     * {@inheritDoc}
     * This tick method is called after a number of initial ticks.
     */
    @Override
    public void tick() {
        if (shiftTime > 0) {
            shiftTime--;
        } else {
            super.tick();
        }
    }
}
