package be.kuleuven.cs.flexsim.solver.heuristic.domain;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.Payment;
import be.kuleuven.cs.flexsim.domain.util.data.DoublePowerCapabilityBand;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class OptaFlexProvider implements QHFlexibilityProvider {
    private static final double HOUR_TO_QUARTER_HOUR = 0.25d;
    private final FlexibilityProvider provider;

    public OptaFlexProvider(FlexibilityProvider provider) {
        this.provider = provider;
    }

    @Override
    public DoublePowerCapabilityBand getFlexibilityActivationRate() {
        return DoublePowerCapabilityBand
                .create(provider.getFlexibilityActivationRate().getDown() * HOUR_TO_QUARTER_HOUR,
                        provider.getFlexibilityActivationRate().getUp() * HOUR_TO_QUARTER_HOUR);
    }

    @Override
    public HourlyFlexConstraints getFlexibilityActivationConstraints() {
        return provider.getFlexibilityActivationConstraints();
    }

    @Override
    public double getMonetaryCompensationValue() {
        return provider.getMonetaryCompensationValue();
    }

    @Override
    public void registerActivation(FlexActivation activation, Payment payment) {
        provider.registerActivation(activation, payment);
    }

    @Override
    public FlexibilityProvider getWrappedProvider() {
        return provider;
    }
}
