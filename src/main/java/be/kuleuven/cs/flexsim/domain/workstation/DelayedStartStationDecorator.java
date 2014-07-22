package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * This decorator adds the functionality of delaying the start of operation
 * (recieving ticks) by a couple of timesteps.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
class DelayedStartStationDecorator extends
        ForwardingStationDecorator<Workstation> {

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
     * {@inheritDoc} This tick method is called after a number of initial ticks.
     */
    @Override
    public void afterTick(int t) {
        if (shiftTimeAfter > 0) {
            shiftTimeAfter--;
        } else {
            super.afterTick(t);
        }
    }

    /**
     * {@inheritDoc} This tick method is called after a number of initial ticks.
     */
    @Override
    public void tick(int t) {
        if (shiftTime > 0) {
            shiftTime--;
        } else {
            super.tick(t);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DelayedStartStationDecorator [shiftTime=")
                .append(shiftTime).append(", hc=").append(this.hashCode())
                .append("]");
        return builder.toString();
    }
}
