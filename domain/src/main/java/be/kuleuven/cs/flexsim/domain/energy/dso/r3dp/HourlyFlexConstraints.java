package be.kuleuven.cs.flexsim.domain.energy.dso.r3dp;

import com.google.auto.value.AutoValue;

/**
 * Concrete flexibility constraint representation.
 * All values are in hours unless specified otherwise.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class HourlyFlexConstraints implements FlexConstraints {

    /**
     * No flex available.
     */
    public static final HourlyFlexConstraints NOFLEX = builder().interActivationTime(0)
            .activationDuration(0)
            .maximumActivations(0).build();
    /**
     * Flex constraints according to Elia's R3DP product with 40 activations, interactivation
     * time of 12 hours and a maximum duration of 2 hours.
     */
    public static final HourlyFlexConstraints R3DP = builder().build();
    private static final int R3DP_MAX_ACTIVATIONS = 40;
    private static final int R3DP_INTERACTIVATION_TIME = 12;
    private static final int R3DP_ACTIVATION_DURATION = 2;

    HourlyFlexConstraints() {
    }

    /**
     * @return A new constraint builder.
     */
    public static Builder builder() {
        return new AutoValue_HourlyFlexConstraints.Builder().interActivationTime(
                R3DP_INTERACTIVATION_TIME).activationDuration(R3DP_ACTIVATION_DURATION)
                .maximumActivations(R3DP_MAX_ACTIVATIONS);
    }

    /**
     * Autovalue builder for hourly flex constraints.
     */
    @AutoValue.Builder
    public abstract static class Builder {

        Builder() {
        }

        /**
         * @param value The min time betweeen two consecutive activations.
         * @return this builder
         */
        public abstract Builder interActivationTime(double value);

        /**
         * @param value The maximum activation duration.
         * @return this builder
         */
        public abstract Builder activationDuration(double value);

        /**
         * @param value The max number of activations per year.
         * @return this builder
         */
        public abstract Builder maximumActivations(double value);

        /**
         * @return the value object with the constraints.
         */
        public abstract HourlyFlexConstraints build();
    }
}
