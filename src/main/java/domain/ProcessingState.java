package domain;

/**
 * The Class ProcessingState.
 */
class ProcessingState implements IStationState {

    /* (non-Javadoc)
     * @see domain.IStationState#handleTick(domain.IStationContext)
     */
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

    /* (non-Javadoc)
     * @see domain.IStationState#isProcessing()
     */
    @Override
    public boolean isProcessing() {
        return true;
    }

}
