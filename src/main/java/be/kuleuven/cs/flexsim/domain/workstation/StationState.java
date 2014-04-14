package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * The Interface IStationState.
 */
interface StationState {

    /**
     * Returns the consumptionRate in this state.
     * 
     * @param remainingSteps
     *            The remaining steps until finished processing in this state.
     * @param totalSteps
     *            The total steps needed for the duration spent in this state.
     * 
     * @return the consumption rate.
     */
    double getVarConsumptionRate(int remainingSteps, int totalSteps);

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
