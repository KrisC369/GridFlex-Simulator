package domain;

interface IStationContext {
	void setState(IStationState newState);

	boolean pushConveyor();

	IResource getCurrentResource();

	void triggerRemainingTicks(int timestep);
}
