package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * The Interface IStationState.
 */
interface StationState {

    /**
     * Returns the consumptionRate in this state.
     * 
     * @return the consumption rate.
     */
    int getConsumptionRate();

    /**
     * Handle tick.
     * 
     * @param context
     *            the context
     */
    void handleTick(WorkstationContext context);

    /**
     * Checks if is processing.
     * 
     * @return true, if is processing
     */
    boolean isProcessing();

}
