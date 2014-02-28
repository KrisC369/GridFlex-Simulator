package domain;

interface IStationState {

    void handleTick(IStationContext context);

    boolean isProcessing();

}
