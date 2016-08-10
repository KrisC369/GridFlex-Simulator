package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.experimentation.tosg.optimal.FlexConstraints;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class FlexProvider {
    private FlexConstraints constraints;
    private double powerRate;

    public FlexProvider(final double powerRate) {
        this(powerRate, FlexConstraints.R3DP);
    }

    public FlexProvider(final double powerRate, final FlexConstraints contract) {
        this.constraints = contract;
        this.powerRate = powerRate;
    }

    public FlexConstraints getActivationConstraints() {
        return constraints;
    }

    public double getPowerRate() {
        return powerRate;
    }
}
