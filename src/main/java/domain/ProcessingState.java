package domain;

public class ProcessingState implements IStationState {

	@Override
	public void handleTick(IStationContext context, int timestep) {
		IResource res = context.getCurrentResource();
		while(res.getNeededProcessTime() > 0 && timestep > 0){
			res.process(1);
			timestep--;
		}
		if(timestep> res.getNeededProcessTime()){
			changestate(context);
			redistributeTick(context,timestep);
		}
		if(res.getNeededProcessTime()==0){
			changestate(context);
		}

	}

	private void redistributeTick(IStationContext context,int timestep) {
		context.triggerRemainingTicks(timestep);
	}

	private void changestate(IStationContext context) {
		context.setState(new ResourceMovingState());
	}

	@Override
	public boolean isProcessing() {
		return true;
	}

}
