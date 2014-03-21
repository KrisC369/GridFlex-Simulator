package domain.workstation;

import com.google.common.base.Optional;

import domain.resource.Resource;

/**
 * This interface represents the methods that the station states can call on the
 * context class.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
interface WorkstationContext {

    /**
     * This method returns the resource currently present in the workstation.
     * This value is <code>Optional</code> and can represent no resource being
     * present.
     * 
     * @return the current resource wrapped in an Optional.
     */
    Optional<Resource> getCurrentResource();

    /**
     * This method pushes the represented conveyer belt forward. Resources get
     * pulled from the in-buffer if there is room in the station and finished
     * resources can be pushed to the out-buffer when finished.
     * 
     * @return true, if successful
     */
    boolean pushConveyer();

    /**
     * Change the state of this Station context instance to the working state.
     */
    void setProcessingState();

    /**
     * Change the state of this Station context instance to the idle state.
     */
    void setResourceMovingState();

}
