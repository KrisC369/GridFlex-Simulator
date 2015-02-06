package be.kuleuven.cs.flexsim.domain.energy.tso;

/**
 * Data class signifying a band of operation concerning the power for an entity.
 * Has an upper limit in up and down area.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class PowerCapabilityBand {
    private final int downMax;
    private final int upMax;

    /**
     * Default constructor.
     *
     * @param downMax
     *            The maximum for the negative part.
     * @param upMax
     *            The maximum for the positive part.
     */
    private PowerCapabilityBand(int downMax, int upMax) {
        this.downMax = downMax;
        this.upMax = upMax;
    }

    /**
     * @return the downMax
     */
    public int getDown() {
        return downMax;
    }

    /**
     * @return the upMax
     */
    public int getUp() {
        return upMax;
    }

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
        return new PowerCapabilityBand(0, 0);
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
    public static PowerCapabilityBand create(int downMax, int upMax) {
        return new PowerCapabilityBand(downMax, upMax);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PowerCapBand [downMax=").append(downMax)
                .append(", upMax=").append(upMax).append("]");
        return builder.toString();
    }

}
