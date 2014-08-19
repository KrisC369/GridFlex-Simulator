package be.kuleuven.cs.flexsim.domain.workstation;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.annotations.VisibleForTesting;

/**
 * A decorator for decorating workstation instances that steerable with a random
 * noise factor on the variable consumption.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
public class RFSteerableStationDecorator extends SteerableStationDecorator
        implements DualModeWorkstation {

    /**
     * Width of the random noise band.
     */
    private final int n;

    private RandomGenerator g = new MersenneTwister();
    private final int high;
    private final int low;
    private boolean isHigh;
    private int offset;

    RFSteerableStationDecorator(ConfigurableWorkstation ws, int high, int low,
            int width) {
        super(ws);
        this.high = high;
        this.low = low;
        this.isHigh = false;
        this.offset = 0;
        this.n = width;
    }

    // TODO decide on speed incr. when switching between high and low
    // consumption. Otherwise
    // remove dependency on steerableStationDec.
    @Override
    public void signalHighConsumption() {
        if (isHigh) {
            throw new IllegalStateException(
                    "This switch is set to high before call to setHigh.");
        }
        this.isHigh = true;
        final int diff = this.high - (this.low + this.getOffset());
        getDelegate().increaseRatedMaxVarECons(diff);
        resetOffset();
    }

    @Override
    public void signalLowConsumption() {
        if (!isHigh) {
            throw new IllegalStateException(
                    "This switch is set to low before call to setLow.");
        }
        this.isHigh = false;
        final int diff = (this.high + this.getOffset()) - this.low;
        getDelegate().decreaseRatedMaxVarECons(diff);
        resetOffset();
    }

    @Override
    public void tick(int t) {
        triggerChange(g.nextInt(n));
        super.tick(t);
    }

    @VisibleForTesting
    void triggerChange(final int r) {
        final int newVal = getTarget() - n / 2 + r;
        final int diff = newVal - (getTarget() + getOffset());
        if (diff > 0) {
            getDelegate().increaseRatedMaxVarECons(diff);
        } else {
            getDelegate().decreaseRatedMaxVarECons(Math.abs(diff));
        }
        this.setOffset(newVal - getTarget());
    }

    private int getTarget() {
        if (isHigh) {
            return this.high;
        }
        return this.low;
    }

    @Override
    public void registerWith(WorkstationRegisterable subject) {
        subject.register((DualModeWorkstation) this);
        super.registerWith(subject);
    }

    /**
     * @return the offset
     */
    private int getOffset() {
        return offset;
    }

    /**
     * @param offset
     *            the offset to set
     */
    private void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @param offset
     *            the offset to set
     */
    private void resetOffset() {
        this.offset = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RFSteerableStation [high=").append(high)
                .append(", low=").append(low).append(", isHigh=")
                .append(isHigh).append(", hc=").append(this.hashCode())
                .append("]");
        return builder.toString();
    }

    @Override
    public void initialize(SimulationContext context) {
        this.g = context.getRandom();
        super.initialize(context);
    }

}
