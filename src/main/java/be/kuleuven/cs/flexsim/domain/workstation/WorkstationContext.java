package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * This interface represents the methods that the station states can call on the
 * context class.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
interface WorkstationContext {

    /**
     * Signals the context to activate the processing of the current resources
     * with the specified amount of steps.
     * 
     * @param steps
     *            the amount of steps to process resources with.
     */
    void processResources(int steps);

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

    /**
     * Returns whether this workstation has any current resources that need more
     * processing.
     * 
     * @return true if workstation possesses current resources that need more
     *         processing.
     */
    boolean hasUnfinishedResources();
}
