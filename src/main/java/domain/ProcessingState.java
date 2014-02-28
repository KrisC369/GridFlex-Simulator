package domain;

public class ProcessingState implements IStationState {

    @Override
    public void handleTick(IStationContext context) {
        IResource res = context.getCurrentResource();
        res.process(1);
        if (res.getNeededProcessTime() == 0) {
            changestate(context);
        }
    }

    private void changestate(IStationContext context) {
        context.setState(new ResourceMovingState());
    }

    @Override
    public boolean isProcessing() {
        return true;
    }

}
