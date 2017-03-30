package be.kuleuven.cs.gridflex.domain.workstation;

/**
 * This decorator adds the functionality of delaying the start of operation
 * (recieving ticks) by a couple of timesteps.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
class DelayedStartStationDecorator
        extends ForwardingStationDecorator<Workstation> {

    private int shiftTime;
    private int shiftTimeAfter;

    /**
     * Default constructor for creating this decorator.
     *
     * @param shift           The amount of time steps to wait.
     * @param workstationImpl The time steps to delay execution.
     */
    DelayedStartStationDecorator(final int shift, final Workstation workstationImpl) {
        super(workstationImpl);
        this.shiftTime = shift;
        this.shiftTimeAfter = shift;
    }

    /**
     * {@inheritDoc} This tick method is called after a number of initial ticks.
     */
    @Override
    public void afterTick(final int t) {
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
    public void tick(final int t) {
        if (shiftTime > 0) {
            shiftTime--;
        } else {
            super.tick(t);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(40);
        builder.append("DelayedStartStation [shiftTime=").append(shiftTime)
                .append(", hc=").append(this.hashCode()).append("]");
        return builder.toString();
    }
}
