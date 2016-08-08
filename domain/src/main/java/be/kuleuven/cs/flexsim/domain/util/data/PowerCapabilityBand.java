package be.kuleuven.cs.flexsim.domain.util.data;

import com.google.auto.value.AutoValue;

/**
 * Data class signifying a band of operation concerning the power for an entity.
 * Has an upper limit in up and down area.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */@AutoValue
public abstract class PowerCapabilityBand {

    /**
     * @return the downMax
     */
    public abstract int getDown();

    /**
     * @return the upMax
     */
    public abstract int getUp();
    /**
     * @return Whether this data value has zero for all params.
     */
    public boolean isZero() {
        return getDown() == 0 && getUp() == 0;
    }

    /**
     * Factory method for empty band.
     *
     * @return a new PowerCapabilityBand object.
     */
    public static PowerCapabilityBand createZero() {
        return new AutoValue_PowerCapabilityBand(0, 0);
    }

    /**
     * Default factory method.
     *
     * @param downMax
     *            The maximum for the negative part.
     * @param upMax
     *            The maximum for the positive part.
     * @return a new PowerCapabilityBand object.
     */
    public static PowerCapabilityBand create(final int downMax, final int upMax) {
        return new AutoValue_PowerCapabilityBand(downMax, upMax);
    }
}
