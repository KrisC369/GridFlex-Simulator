package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * 
 * This implementation represents a steerable workstation where one can steer
 * the ratio between Fixed, variable consumption and processing speed. Examples
 * include but are not limited to: Industrial freezers.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
public interface TradeofSteerableWorkstation extends Workstation {
    /**
     * Sets the controllable instance's trade off metric between speed of
     * operation and energy consumption to favor speed of operation.
     * 
     * @param consumptionShift
     *            the amount to shift towards consumption.
     * @param speedShift
     *            the amount to shift towards speed
     * @throws IllegalArgumentException
     *             the percentage is not between 0 and 1.
     */
    void favorSpeedOverFixedEConsumption(int consumptionShift, int speedShift);

    /**
     * Sets the controllable instance's trade off metric between speed of
     * operation and energy consumption to favor low energy consumption.
     * 
     * @param consumptionShift
     *            the amount to shift towards consumption.
     * @param speedShift
     *            the amount to shift towards speed
     * @throws IllegalArgumentException
     *             the percentage is not between 0 and 1.
     */
    void favorFixedEConsumptionOverSpeed(int consumptionShift, int speedShift);
}
