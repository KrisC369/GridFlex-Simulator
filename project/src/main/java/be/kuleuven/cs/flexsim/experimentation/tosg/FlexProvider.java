package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.experimentation.tosg.optimal.FlexConstraints;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class FlexProvider {
    private FlexConstraints constraints;
    private double powerRate;

    public FlexProvider(final double powerRate) {
        this.constraints = FlexConstraints.NOFLEX;
        this.powerRate = powerRate;
    }

    //    @Override
    //    public void signalTarget(final int timestep, final int target) {
    //
    //    }
    //
    //    @Override
    //    public PowerCapabilityBand getPowerCapacity() {
    //        return null;
    //    }

    public FlexConstraints getActivationConstraints() {
        return constraints;
    }

    public double getPowerRate() {
        return powerRate;
    }
}
