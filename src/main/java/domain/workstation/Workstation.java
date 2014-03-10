package domain.workstation;

import simulation.ISimulationContext;

import com.google.common.annotations.VisibleForTesting;

import domain.Buffer;
import domain.IResource;

/**
 * Main workstation class representing machines that perform work and consume
 * energy.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class Workstation implements IWorkstation, IStationContext {

    /** The input buffer. */
    private final Buffer<IResource> inputBuff;
    
    /** The output buffer. */
    private final Buffer<IResource> outputBuff;
    
    /** The state of this workstation. */
    private IStationState state;
    
    /** The current resource. */
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
    private Workstation(Buffer<IResource> bufferIn, Buffer<IResource> bufferOut) {
        this.inputBuff = bufferIn;
        this.outputBuff = bufferOut;
        this.state = new ResourceMovingState();
    }


    /* (non-Javadoc)
     * @see domain.workstation.IWorkstation#getProcessedItemsCount()
     */
    @Override
    public int getProcessedItemsCount() {
        return 0;
    }

    /* (non-Javadoc)
     * @see domain.workstation.IWorkstation#isIdle()
     */
    @Override
    public boolean isIdle() {
        return !state.isProcessing();
    }

    /* (non-Javadoc)
     * @see simulation.ISimulationComponent#initialize(simulation.ISimulationContext)
     */
    @Override
    public void initialize(ISimulationContext context) {
    }

    /* (non-Javadoc)
     * @see simulation.ISimulationComponent#tick()
     */
    @Override
    public void tick() {
        state.handleTick(this);

    }

    /* (non-Javadoc)
     * @see domain.IStationContext#setState(domain.IStationState)
     */
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

    /* (non-Javadoc)
     * @see domain.IStationContext#getCurrentResource()
     */
    @Override
    public IResource getCurrentResource() {

        return currentResource;
    }

    @VisibleForTesting 
    void setCurrentResource(IResource res) {
        if (null != currentResource) {
            throw new IllegalStateException();
        }
        this.currentResource = res;
    }

    /* (non-Javadoc)
     * @see domain.IStationContext#pushConveyer()
     */
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
    
    public static IWorkstation create(Buffer<IResource> bufferIn, Buffer<IResource> bufferOut){
        return new Workstation(bufferIn, bufferOut);
    }
}
