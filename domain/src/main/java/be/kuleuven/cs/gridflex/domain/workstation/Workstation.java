package be.kuleuven.cs.gridflex.domain.workstation;

import be.kuleuven.cs.gridflex.domain.energy.consumption.EnergyConsumptionTrackable;
import be.kuleuven.cs.gridflex.util.visitor.Visitable;

/**
 * Workstation API for public operations on workstation components. Workstations
 * represent machines that perform work and consume energy.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface Workstation extends
                             EnergyConsumptionTrackable, Visitable<WorkstationVisitor> {

    /**
     * Return the amount of items that has been processed by this workstation.
     *
     * @return the processed items count
     */
    int getProcessedItemsCount();

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
    int getRatedCapacity();

    /**
     * Returns the rate of processing items. This is calculated as: rated
     * capacity of the station / time it takes the station to finish a resource.
     *
     * @return The current processing rate.
     */
    double getProcessingRate();
}
