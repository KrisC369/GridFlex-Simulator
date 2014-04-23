package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * Represents instances that allow curtailment of some sorts.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface Curtailable {

    /**
     * Activate curtailment functionality.
     */
    void doFullCurtailment();

    /**
     * Restore the previous uncurtailed state.
     */
    void restore();

    /**
     * Returns whether this instance is in curtailment mode or not.
     * 
     * @return whether this instances is being curtailed.
     */
    boolean isCurtailed();

    /**
     * Sets the controllable instance's trade off metric between speed of
     * operation and energy consumption to favor speed of operation or low
     * energy consumption.
     * 
     * @param shift
     *            the amount to shift.
     * 
     * @param speed
     *            favor for speed if true.
     * @throws IllegalArgumentException
     *             the percentage is not between 0 and 1.
     */
    void setSpeedVsEConsumptionRatio(int shift, boolean speed);

}
