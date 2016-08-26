package be.kuleuven.cs.flexsim.domain.energy.dso.r3dp;

import be.kuleuven.cs.flexsim.domain.util.data.DoublePowerCapabilityBand;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A provider of flexibility with activation constraints.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class FlexProvider implements FlexibilityProvider {
    private static final double FIXED_PRICE = 35.4;
    private final HourlyFlexConstraints constraints;
    private final double powerRate;
    private final List<FlexActivation> activations;
    private long runningCompensationValue;

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
        this.activations = Lists.newArrayList();
    }

    @Override
    public DoublePowerCapabilityBand getFlexibilityActivationRate() {
        return DoublePowerCapabilityBand.create(0, powerRate);
    }

    @Override
    public HourlyFlexConstraints getFlexibilityActivationConstraints() {
        return constraints;
    }

    @Override
    public long getMonetaryCompensationValue() {
        return runningCompensationValue;
    }

    @Override
    public void registerActivation(FlexActivation activation) {
        checkActivation(activation);
        addActivation(activation);
        registerCompensation(activation);
    }

    private void addActivation(FlexActivation activation) {
        this.activations.add(activation);
    }

    private void registerCompensation(FlexActivation activation) {
        runningCompensationValue += activation.getEnergyVolume() * FIXED_PRICE;
    }

    private void checkActivation(FlexActivation activation) {
        //TODO check duration,
        //TODO check interarrival before and after,
        //TODO check rate or volume.
    }

}
