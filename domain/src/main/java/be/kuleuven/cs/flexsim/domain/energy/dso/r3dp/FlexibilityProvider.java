package be.kuleuven.cs.flexsim.domain.energy.dso.r3dp;

import be.kuleuven.cs.flexsim.domain.util.Payment;
import be.kuleuven.cs.flexsim.domain.util.data.DoublePowerCapabilityBand;

/**
 * Represents an entity capable of supplying flexibility under real world operational constraints.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface FlexibilityProvider {
    /**
     * @return The amount of flexible power that this entity is capable of delivering in both
     * injection and offtake directions.
     */
    DoublePowerCapabilityBand getFlexibilityActivationRate();

    /**
     * @return The constraints regarding the activation of flexibility present on site.
     */
    HourlyFlexConstraints getFlexibilityActivationConstraints();

    /**
     * @return The amaount of monetary compensation for activating flexibility.
     */
    double getMonetaryCompensationValue();

    /**
     * Register an activation event for this Flexibility provider.
     */
    void registerActivation(FlexActivation activation, Payment payment);
}
