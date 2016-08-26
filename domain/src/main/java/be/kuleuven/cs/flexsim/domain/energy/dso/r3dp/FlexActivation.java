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

    public abstract double getEnd();

    public abstract double getEnergyVolume();

    public static FlexActivation create(double start, double end, double volume) {
        return new AutoValue_FlexActivation(start, end, volume);
    }
}
