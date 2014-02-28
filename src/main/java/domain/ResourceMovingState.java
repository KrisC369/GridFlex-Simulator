package domain;

public class ResourceMovingState implements IStationState {

	@Override
	public void handleTick(IStationContext context, int timestep) {
		boolean succesfull = context.pushConveyor();
		if(succesfull){
			context.setState(new ProcessingState());
		}
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

}
