package domain;

public class ResourceMovingState implements IStationState {

    @Override
    public void handleTick(IStationContext context) {
        boolean succesfull = context.pushConveyor();
        if (succesfull) {
            changeState(context);
        }
    }

    private void changeState(IStationContext context) {
        context.setState(new ProcessingState());
    }

    @Override
    public boolean isProcessing() {
        return false;
    }

}
