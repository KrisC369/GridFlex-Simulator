package domain.workstation;


/**
 * The Class ResourceMovingState .
 */
class ResourceMovingState implements IStationState {

    /* (non-Javadoc)
     * @see domain.IStationState#handleTick(domain.IStationContext)
     */
    @Override
    public void handleTick(IStationContext context) {
        boolean succesfull = context.pushConveyer();
        if (succesfull) {
            changeState(context);
        }
    }

    private void changeState(IStationContext context) {
        context.setState(new ProcessingState());
    }

    /* (non-Javadoc)
     * @see domain.IStationState#isProcessing()
     */
    @Override
    public boolean isProcessing() {
        return false;
    }

}
