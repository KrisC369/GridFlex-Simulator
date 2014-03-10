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
    private IStationState currentState;
    private final IStationState processingState;

    private final IStationState resourceMovingState;

    /** The current resource. */
    private IResource currentResource;

    private int totalConsumption;

    private int lastConsumption;

    private int processedCount;

    /**
     * Constructor that creates a workstation instance from an in and an out
     * buffer.
     * 
     * @param bufferIn
     *            The In buffer.
     * @param bufferOut
     *            The Out buffer.
     */
    @VisibleForTesting
    Workstation(Buffer<IResource> bufferIn, Buffer<IResource> bufferOut,
            int idle, int working) {
        this.inputBuff = bufferIn;
        this.outputBuff = bufferOut;
        this.processingState = new StationState.Processing(working);
        this.resourceMovingState = new StationState.ResourceMoving(idle);
        this.currentState = resourceMovingState;
        this.totalConsumption = 0;
        this.processedCount = 0;
        this.lastConsumption = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.IStationContext#getCurrentResource()
     */
    @Override
    public IResource getCurrentResource() {

        return currentResource;
    }

    private IStationState getCurrentState() {
        return this.currentState;
    }

    private Buffer<IResource> getInputBuffer() {
        return inputBuff;
    }

    @Override
    public int getLastStepConsumption() {
        return lastConsumption;
    }

    private Buffer<IResource> getOutputBuffer() {
        return outputBuff;
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.workstation.IWorkstation#getProcessedItemsCount()
     */
    @Override
    public int getProcessedItemsCount() {
        return this.processedCount;
    }

    @Override
    public int getTotalConsumption() {
        return this.totalConsumption;
    }

    private void increaseTotalConsumption(int consumptionRate) {
        this.totalConsumption += consumptionRate;
    }

    private void incrementProcessedCount() {
        this.processedCount++;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * simulation.ISimulationComponent#initialize(simulation.ISimulationContext)
     */
    @Override
    public void initialize(ISimulationContext context) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.workstation.IWorkstation#isIdle()
     */
    @Override
    public boolean isIdle() {
        return !currentState.isProcessing();
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.IStationContext#pushConveyer()
     */
    @Override
    public boolean pushConveyer() {
        if (null != getCurrentResource()) {
            getOutputBuffer().push(getCurrentResource());
            this.currentResource = null;
            incrementProcessedCount();
        }
        if (!getInputBuffer().isEmpty()) {
            this.setCurrentResource(getInputBuffer().pull());
            return true;
        }
        return false;
    }

    @VisibleForTesting
    void setCurrentResource(IResource res) {
        if (null != currentResource) { throw new IllegalStateException(); }
        this.currentResource = res;
    }

    private void setLastConsumption(int rate) {
        this.lastConsumption = rate;
    }

    @Override
    public void setProcessingState() {
        this.currentState = processingState;

    }

    @Override
    public void setResourceMovingState() {
        this.currentState = resourceMovingState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see simulation.ISimulationComponent#tick()
     */
    @Override
    public void tick() {
        int rate = getCurrentState().getConsumptionRate();
        increaseTotalConsumption(rate);
        setLastConsumption(rate);
        currentState.handleTick(this);
    }

    /**
     * Factory method for workstations that consume energy..
     * 
     * @param in
     *            The inputbuffer instance.
     * @param out
     *            The outputbuffer instance.
     * @param idle
     *            The energy consumption in idle state.
     * @param working
     *            The energy consumption in working state.
     * @return A Ready to use IWorkstation object.
     */
    public static IWorkstation createConsuming(Buffer<IResource> in,
            Buffer<IResource> out, int idle, int working) {
        return new Workstation(in, out, idle, working);
    }

    /**
     * Factory method for default workstations without energy consumption.
     * 
     * @param bufferIn
     *            The inputbuffer instance.
     * @param bufferOut
     *            The outputbuffer instance.
     * @return A Ready to use IWorkstation object.
     */
    public static IWorkstation createDefault(Buffer<IResource> bufferIn,
            Buffer<IResource> bufferOut) {
        return new Workstation(bufferIn, bufferOut, 0, 0);
    }
}
