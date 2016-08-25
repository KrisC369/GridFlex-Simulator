package be.kuleuven.cs.flexsim.domain.util.data;

import com.google.auto.value.AutoValue;

/**
 * Data class signifying a band of operation concerning the power for an entity.
 * Has an upper limit in up and down area.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@AutoValue
public abstract class DoublePowerCapabilityBand {

    DoublePowerCapabilityBand() {
    }

    /**
     * @return the downMax
     */
    public abstract double getDown();

    /**
     * @return the upMax
     */
    public abstract double getUp();

    /**
     * @return Whether this data value has zero for all params.
     */
    public boolean isZero() {
        return getDown() == 0 && getUp() == 0;
    }

    /**
     * Factory method for empty band.
     *
     * @return a new IntPowerCapabilityBand object.
     */
    public static DoublePowerCapabilityBand createZero() {
        return new AutoValue_DoublePowerCapabilityBand(0, 0);
    }

    /**
     * Default factory method.
     *
     * @param downMax The maximum for the negative part.
     * @param upMax   The maximum for the positive part.
     * @return a new IntPowerCapabilityBand object.
     */
    public static DoublePowerCapabilityBand create(final double downMax, final double upMax) {
        return new AutoValue_DoublePowerCapabilityBand(downMax, upMax);
    }
}
