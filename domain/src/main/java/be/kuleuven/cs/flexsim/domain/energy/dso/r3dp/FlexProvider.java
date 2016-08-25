package be.kuleuven.cs.flexsim.domain.energy.dso.r3dp;

import be.kuleuven.cs.flexsim.domain.util.data.DoublePowerCapabilityBand;

/**
 * A provider of flexibility with activation constraints.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class FlexProvider implements FlexibilityProvider {
    private final HourlyFlexConstraints constraints;
    private final double powerRate;

    /**
     * Constructor
     *
     * @param powerRate The powerrate this provider can offer.
     */
    public FlexProvider(final double powerRate) {
        this(powerRate, HourlyFlexConstraints.R3DP);
    }

    /**
     * Constructor with contract.
     *
     * @param powerRate The powerrate this provider can offer.
     * @param contract  The contract containing the flexibility constraints agreed upon.
     */
    public FlexProvider(final double powerRate, final HourlyFlexConstraints contract) {
        this.constraints = contract;
        this.powerRate = powerRate;
    }

    @Override
    public DoublePowerCapabilityBand getFlexibilityActivationRate() {
        return DoublePowerCapabilityBand.create(0, powerRate);
    }

    @Override
    public HourlyFlexConstraints getFlexibilityActivationConstraints() {
        return constraints;
    }
}
