package be.kuleuven.cs.flexsim.domain.workstation;

import org.slf4j.LoggerFactory;

/**
 * This station decorator allows for curtailment functionality for workstations.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
class CurtailableStationDecorator<T extends Workstation> extends
                                                         ForwardingStationDecorator<T>
        implements CurtailableWorkstation {

    private boolean curtailed;

    /**
     * Default constructor for creating this decorator.
     *
     * @param delegate
     */
    CurtailableStationDecorator(final T delegate) {
        super(delegate);
        this.curtailed = false;
    }

    @Override
    public void doFullCurtailment() {
        if (isCurtailed()) {
            throw new IllegalStateException();
        }
        logCurtailment();
        curtailed = true;
    }

    @Override
    public void restore() {
        if (!isCurtailed()) {
            throw new IllegalStateException();
        }
        logRestoration();
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
    public void afterTick(final int t) {
        if (!isCurtailed()) {
            super.afterTick(t);
        }
    }

    /**
     * Only defers ticks when not in curtailment mode. {@inheritDoc}
     */
    @Override
    public void tick(final int t) {
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
    public void acceptVisitor(final WorkstationVisitor subject) {
        subject.register(this);
        super.acceptVisitor(subject);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(45);
        builder.append("CurtailableWorkstation [curtailed=").append(curtailed)
                .append(", hc=").append(this.hashCode()).append("]");
        return builder.toString();
    }

    @Override
    public double getProcessingRate() {
        if (isCurtailed()) {
            return 0;
        }
        return getDelegate().getProcessingRate();
    }

    private void logCurtailment() {
        LoggerFactory.getLogger(CurtailableWorkstation.class)
                .debug("Full curtailment active on {}", this);

    }

    private void logRestoration() {
        LoggerFactory.getLogger(CurtailableWorkstation.class)
                .debug("Restoring curtailment on {}", this);
    }
}
