package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;

/**
 * Workstation API for public operations on workstation components. Workstations
 * represent machines that perform work and consume energy.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface Workstation extends SimulationComponent,
        Registering<WorkstationRegisterable> {

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
     * Returns the current average energy consumption amount.
     *
     * @return The avg energy consumption.
     */
    double getAverageConsumption();

    /**
     * Returns wheter this machine is performing work during this time step or
     * not.
     * 
     * @return true if performing work during this time step.
     */
    boolean isIdle();

    /**
     * Returns the capacity of this workstation as rated by the specs.
     * 
     * @return the capacity.
     */
    public abstract int getRatedCapacity();

    /**
     * Returns the rate of processing items.
     * 
     * @return The current processing rate.
     */
    double getProcessingRate();
}
