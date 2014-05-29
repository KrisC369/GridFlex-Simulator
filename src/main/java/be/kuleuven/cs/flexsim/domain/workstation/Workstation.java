package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;

/**
 * Workstation API for public operations on workstation components. Workstations
 * represent machines that perform work and consume energy.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface Workstation extends SimulationComponent {

    /**
     * Returns the energy consumption amount during the last step.
     * 
     * @return an int representing consumption amount.
     */
    double getLastStepConsumption();

    /**
     * Return the amount of items that has been processed by this workstation.
     * 
     * @return the processed items count
     */
    int getProcessedItemsCount();

    /**
     * Returns the total energy consumption amount.
     * 
     * @return the total energy consumed.
     */
    double getTotalConsumption();

    /**
     * Returns wheter this machine is performing work during this time step or
     * not.
     * 
     * @return true if performing work during this time step.
     */
    boolean isIdle();

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
