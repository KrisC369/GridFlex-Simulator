package domain;

import simulation.ISimulationComponent;
import simulation.ISimulationContext;

/**
 * Main workstation class representing machines that perform work and consume
 * energy.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class Workstation implements ISimulationComponent, IStationContext {

    private final Buffer<IResource> inputBuff;
    private final Buffer<IResource> outputBuff;
    private IStationState state;
    private IResource currentResource;

    /**
     * Constructor that creates a workstation instance from an in and an out
     * buffer.
     * 
     * @param bufferIn
     *            The In buffer.
     * @param bufferOut
     *            The Out buffer.
     */
    public Workstation(Buffer<IResource> bufferIn, Buffer<IResource> bufferOut) {
        this.inputBuff = bufferIn;
        this.outputBuff = bufferOut;
        this.state = new ResourceMovingState();
    }

    /**
     * Return the amount of items that has been processed by this workstation.
     * 
     * @return
     */
    public int getProcessedItemsCount() {
        return 0;
    }

    /**
     * Returns wheter this machine is performing work during this time step or
     * not.
     * 
     * @return true if performing work during this time step.
     */
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

    /**
     * Set the current resource present in the workstation. Only succeeds if no
     * other resource is present.
     * 
     * @param res
     *            The resource to set.
     */
    public void setCurrentResource(IResource res) {
        if (null != currentResource) {
            throw new IllegalStateException();
        }
        this.currentResource = res;
    }

    @Override
    public boolean pushConveyer() {
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
