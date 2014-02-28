package domain;

import simulation.ISimulationComponent;
import simulation.ISimulationContext;

public class Workstation implements ISimulationComponent, IStationContext {

    private final Buffer<IResource> inputBuff;
    private final Buffer<IResource> outputBuff;
    private IStationState state;
    private IResource currentResource;

    public Workstation(Buffer<IResource> bufferIn, Buffer<IResource> bufferOut) {
        this.inputBuff = bufferIn;
        this.outputBuff = bufferOut;
        this.state = new ResourceMovingState();
    }

    public int getProcessedItemsCount() {
        return 0;
    }

    public boolean isIdle() {
        return !state.isProcessing();
    }

    @Override
    public void initialize(ISimulationContext context) {

    }

    @Override
    public void tick() {
        state.handleTick(this);

    }

    @Override
    public void setState(IStationState newState) {
        this.state = newState;

    }

    private Buffer<IResource> getInputBuffer() {
        return inputBuff;
    }

    private Buffer<IResource> getOutputBuffer() {
        return outputBuff;
    }

    @Override
    public IResource getCurrentResource() {

        return currentResource;
    }

    public void setCurrentResource(IResource res) {
        if (null != currentResource) {
            throw new IllegalStateException();
        }
        this.currentResource = res;
    }

    @Override
    public boolean pushConveyor() {
        if (null != getCurrentResource()) {
            getOutputBuffer().push(getCurrentResource());
            this.currentResource = null;
        }
        if (!getInputBuffer().isEmpty()) {
            this.setCurrentResource(getInputBuffer().pull());
            return true;
        }
        return false;
    }
}
