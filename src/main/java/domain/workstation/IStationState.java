package domain.workstation;

/**
 * The Interface IStationState.
 */
interface IStationState {

    /**
     * Handle tick.
     *
     * @param context the context
     */
    void handleTick(IStationContext context);

    /**
     * Checks if is processing.
     *
     * @return true, if is processing
     */
    boolean isProcessing();

    /**
     * Returns the consumptionRate in this state.
     * @return the consumption rate.
     */
     int getConsumptionRate();

}
