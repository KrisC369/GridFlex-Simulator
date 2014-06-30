package be.kuleuven.cs.flexsim.domain.workstation;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * A decorator for decorating workstation instances that steerable with a random
 * noise factor on the variable consumption.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class RFSteerableStationDecorator extends SteerableStationDecorator
        implements DualModeWorkstation {

    /**
     * With of the random noise band.
     */
    public static final int N = 100;
    private final RandomGenerator g = new MersenneTwister();
    private final int high;
    private final int low;
    private boolean isHigh;
    private int offset;

    RFSteerableStationDecorator(ConfigurableWorkstation ws, int high, int low) {
        super(ws);
        this.high = high;
        this.low = low;
        this.isHigh = false;
        this.offset = 0;
    }

    // TODO decide on speed incr.
    @Override
    public void signalHighConsumption() {
        if (isHigh) {
            throw new IllegalStateException(
                    "This switch is set to high before call to setHigh.");
        }
        this.isHigh = true;
        final int diff = this.high - (this.low + this.offset);
        getDelegate().increaseRatedMaxVarECons(diff);
    }

    @Override
    public void signalLowConsumption() {
        if (!isHigh) {
            throw new IllegalStateException(
                    "This switch is set to low before call to setLow.");
        }
        this.isHigh = false;
        final int diff = (this.high + this.offset) - this.low;
        getDelegate().decreaseRatedMaxVarECons(diff);
    }

    @Override
    public void tick(int t) {
        triggerChange(g.nextInt(N));
        super.tick(t);
    }

    void triggerChange(final int r) {
        final int newVal = getTarget() - N / 2 + r;
        final int diff = newVal - (getTarget() + offset);
        if (diff > 0) {
            getDelegate().increaseRatedMaxVarECons(diff);
        } else {
            getDelegate().decreaseRatedMaxVarECons(Math.abs(diff));
        }
        this.offset = newVal - getTarget();
    }

    private int getTarget() {
        if (isHigh) {
            return this.high;
        }
        return this.low;
    }

    @Override
    public void registerWith(Registerable subject) {
        subject.register((DualModeWorkstation) this);
    }
}
