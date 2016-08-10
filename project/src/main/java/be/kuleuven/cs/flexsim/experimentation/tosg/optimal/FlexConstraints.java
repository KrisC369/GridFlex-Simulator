package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import com.google.auto.value.AutoValue;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class FlexConstraints {

    public static final FlexConstraints NOFLEX = builder().interActivationTime(0)
            .activationDuration(0)
            .maximumActivations(0).build();
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
    abstract static class Builder {
        abstract Builder interActivationTime(double value);

        abstract Builder activationDuration(double value);

        abstract Builder maximumActivations(double value);

        abstract FlexConstraints build();
    }

    public static Builder builder() {
        return new AutoValue_FlexConstraints.Builder().interActivationTime(12).activationDuration(2)
                .maximumActivations(40);
    }
}
