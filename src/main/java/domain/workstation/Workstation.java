package domain.workstation;

import javax.annotation.Nullable;

import simulation.ISimulationContext;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import domain.resource.IResource;
import domain.util.Buffer;

/**
 * Main workstation class representing machines that perform work and consume
 * energy.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class Workstation implements IWorkstation, IStationContext {

    private final Buffer<IResource> inputBuff;
    private final Buffer<IResource> outputBuff;
    private final IStationState resourceMovingState;
    private final IStationState processingState;
    private IStationState currentState;
    private Optional<IResource> currentResource;
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
        this.currentResource = Optional.absent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.IStationContext#getCurrentResource()
     */
    @Override
    public Optional<IResource> getCurrentResource() {
        
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
        if (getCurrentResource().isPresent()) {
            getOutputBuffer().push(getCurrentResource().get());
            resetCurrentResource();
            incrementProcessedCount();
        }
        if (!getInputBuffer().isEmpty()) {
            this.changeCurrentResource(getInputBuffer().pull());
            return true;
        }
        return false;
    }

    private void resetCurrentResource(){
        this.currentResource = Optional.absent();
    }
    
    @VisibleForTesting
    void changeCurrentResource(IResource res) {
        if (currentResource.isPresent()) {
            throw new IllegalStateException();
        }
        this.currentResource = Optional.of(res);
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
