package be.kuleuven.cs.gridflex.domain.workstation;

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
     * @param context
     *            The context of this state specific operation.
     * @return the consumption rate.
     */
    double getVarConsumptionRate(int remainingSteps, int totalSteps,
            WorkstationContext context);

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

    /**
     * Returns the consumption model for this state.
     * 
     * @return the consumption model.
     */
    ConsumptionModel getModel();
}
