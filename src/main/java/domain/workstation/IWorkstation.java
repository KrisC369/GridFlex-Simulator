package domain.workstation;

import simulation.ISimulationComponent;

/**
 * Workstation API for public operations on workstation components.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface IWorkstation extends ISimulationComponent {

    /**
     * Returns wheter this machine is performing work during this time step or
     * not.
     * 
     * @return true if performing work during this time step.
     */
    boolean isIdle();

    /**
     * Return the amount of items that has been processed by this workstation.
     *
     * @return the processed items count
     */
    int getProcessedItemsCount();

}
