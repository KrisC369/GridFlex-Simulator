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

    // /**
    // * Sets the maximum bound for the variable consumption in the current
    // state.
    // *
    // * @param amount
    // * the new maximum.
    // */
    // void setMaxVariableConsumption(int amount);
    //
    // /**
    // * Get the maximum of the variable consumption rate for this state.
    // *
    // * @return the maximum of variable consumption.
    // */
    // int getMaxVariableConsumption();

    /**
     * Returns the consumption model for this state.
     * 
     * @return the consumption model.
     */
    ConsumptionModel getModel();
}
