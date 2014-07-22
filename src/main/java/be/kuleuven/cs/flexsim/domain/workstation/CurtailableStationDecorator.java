package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * This station decorator allows for curtailment functionality for workstations.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
class CurtailableStationDecorator<T extends Workstation> extends
        ForwardingStationDecorator<T> implements CurtailableWorkstation {

    private boolean curtailed;

    /**
     * Default constructor for creating this decorator.
     * 
     * @param delegate
     */
    CurtailableStationDecorator(T delegate) {
        super(delegate);
        this.curtailed = false;
    }

    @Override
    public void doFullCurtailment() {
        if (isCurtailed()) {
            throw new IllegalStateException();
        }
        curtailed = true;
    }

    @Override
    public void restore() {
        if (!isCurtailed()) {
            throw new IllegalStateException();
        }
        curtailed = false;
    }

    @Override
    public boolean isCurtailed() {
        return curtailed;
    }

    /**
     * Only defers ticks when not in curtailment mode. {@inheritDoc}
     */
    @Override
    public void afterTick(int t) {
        if (!isCurtailed()) {
            super.afterTick(t);
        }
    }

    /**
     * Only defers ticks when not in curtailment mode. {@inheritDoc}
     */
    @Override
    public void tick(int t) {
        if (!isCurtailed()) {
            super.tick(t);
        }
    }

    @Override
    public double getLastStepConsumption() {
        if (isCurtailed()) {
            return 0;
        }
        return getDelegate().getLastStepConsumption();
    }

    @Override
    public void registerWith(WorkstationRegisterable subject) {
        subject.register(this);
        super.registerWith(subject);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CurtailableStationDecorator [curtailed=")
                .append(curtailed).append(", hc=").append(this.hashCode())
                .append("]");
        return builder.toString();
    }
}
