package be.kuleuven.cs.gridflex.domain.energy.dso.r3dp;

import com.google.auto.value.AutoValue;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class FlexActivation {

    public static final FlexActivation EMPTY = create(0, 0, 0);

    FlexActivation() {
    }

    public abstract double getStart();

    public abstract double getDuration();

    public abstract double getEnergyVolume();

    public static FlexActivation create(double start, double duration, double volume) {
        return new AutoValue_FlexActivation(start, duration, volume);
    }
}
