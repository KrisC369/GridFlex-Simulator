package domain;

interface IStationState {

	void handleTick(IStationContext context, int timestep);

	boolean isProcessing();

}
