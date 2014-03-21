package domain.workstation;

import simulation.SimulationContext;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import domain.resource.Resource;
import domain.util.Buffer;

/**
 * Main workstation class representing machines that perform work and consume
 * energy.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class WorkstationImpl implements Workstation, WorkstationContext {

    private final Buffer<Resource> inputBuff;

    private final Buffer<Resource> outputBuff;

    private final StationState resourceMovingState;
    private final StationState processingState;
    private StationState currentState;
    private Optional<Resource> currentResource;
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
    WorkstationImpl(Buffer<Resource> bufferIn, Buffer<Resource> bufferOut,
            int idle, int working) {
        this.inputBuff = bufferIn;
        this.outputBuff = bufferOut;
        this.processingState = new StationStateImpl.Processing(working);
        this.resourceMovingState = new StationStateImpl.ResourceMoving(idle);
        this.currentState = resourceMovingState;
        this.totalConsumption = 0;
        this.processedCount = 0;
        this.lastConsumption = 0;
        this.currentResource = Optional.absent();
    }

    @Override
    public void afterTick() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.IStationContext#getCurrentResource()
     */
    @Override
    public Optional<Resource> getCurrentResource() {

        return currentResource;
    }

    @Override
    public int getLastStepConsumption() {
        return lastConsumption;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * simulation.ISimulationComponent#initialize(simulation.ISimulationContext)
     */
    @Override
    public void initialize(SimulationContext context) {
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
        setLastConsumption(0);
        return false;
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

    @VisibleForTesting
    void changeCurrentResource(Resource res) {
        if (currentResource.isPresent()) {
            throw new IllegalStateException();
        }
        this.currentResource = Optional.of(res);
    }

    private StationState getCurrentState() {
        return this.currentState;
    }

    private Buffer<Resource> getInputBuffer() {
        return inputBuff;
    }

    private Buffer<Resource> getOutputBuffer() {
        return outputBuff;
    }

    private void increaseTotalConsumption(int consumptionRate) {
        this.totalConsumption += consumptionRate;
    }

    private void incrementProcessedCount() {
        this.processedCount++;

    }

    private void resetCurrentResource() {
        this.currentResource = Optional.absent();
    }

    private void setLastConsumption(int rate) {
        this.lastConsumption = rate;
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
    public static Workstation createConsuming(Buffer<Resource> in,
            Buffer<Resource> out, int idle, int working) {
        return new WorkstationImpl(in, out, idle, working);
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
    public static Workstation createDefault(Buffer<Resource> bufferIn,
            Buffer<Resource> bufferOut) {
        return new WorkstationImpl(bufferIn, bufferOut, 0, 0);
    }
}
