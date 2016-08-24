package be.kuleuven.cs.flexsim.domain.energy.dso.offline.r3dp;

import com.google.auto.value.AutoValue;

/**
 * Representation of the constraint concerning flexibility activation.
 * This is similar to a contract between provider and SO stating the allowed boundaries of
 * operation when dealing with energy flexibility.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class FlexConstraints {
    /**
     * No flex available.
     */
    public static final FlexConstraints NOFLEX = builder().interActivationTime(0)
            .activationDuration(0)
            .maximumActivations(0).build();
    /**
     * Flex constraints according to Elia's R3DP product.
     */
    public static final FlexConstraints R3DP = builder().build();

    /**
     * @return The number of time steps allowed between activations.
     */
    public abstract double getInterActivationTime();

    /**
     * @return The maximum number of consecutive time steps activation is allowed.
     */
    public abstract double getActivationDuration();

    /**
     * @return The maximum amount of allowed activations
     */
    public abstract double getMaximumActivations();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder interActivationTime(double value);

        public abstract Builder activationDuration(double value);

        public abstract Builder maximumActivations(double value);

        public abstract FlexConstraints build();
    }

    public static Builder builder() {
        return new AutoValue_FlexConstraints.Builder().interActivationTime(12).activationDuration(2)
                .maximumActivations(40);
    }
}
