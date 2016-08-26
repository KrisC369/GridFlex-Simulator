package be.kuleuven.cs.flexsim.domain.energy.dso.r3dp;

import com.google.auto.value.AutoValue;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class FlexActivation {
    FlexActivation() {
    }

    public abstract double getStart();

    public abstract double getDuration();

    public abstract double getEnergyVolume();

    public static FlexActivation create(double start, double duration, double volume) {
        return new AutoValue_FlexActivation(start, duration, volume);
    }
}
