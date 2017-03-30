package be.kuleuven.cs.gridflex.domain.workstation;

import be.kuleuven.cs.gridflex.domain.resource.Resource;
import be.kuleuven.cs.gridflex.domain.util.Buffer;
import be.kuleuven.cs.gridflex.domain.util.CollectionUtils;
import be.kuleuven.cs.gridflex.simulation.SimulationComponent;
import be.kuleuven.cs.gridflex.simulation.SimulationContext;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Main workstation class representing machines that perform work and consume
 * energy.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
class WorkstationImpl implements ConfigurableWorkstation {

    private final Buffer<Resource> inputBuff;
    private final Buffer<Resource> outputBuff;
    private final StationState resourceMovingState;
    private final StationState processingState;
    private StationState currentState;
    private List<Resource> currentResource;
    private double totalConsumption;
    private double lastConsumption;
    private int processedCount;
    private int fixedECons;
    private int ratedMaxVarECons;
    private final int capacity;
    private Processor proc = new ProcessorImpl();
    private final WorkstationContext stateContext = new StateContext();
    private double lastProcessingRate;

    /**
     * Constructor that creates a workstation instance from an in and an out
     * buffer.
     *
     * @param bufferIn  The In buffer.
     * @param bufferOut The Out buffer.
     */
    @VisibleForTesting
    WorkstationImpl(final Buffer<Resource> bufferIn, final Buffer<Resource> bufferOut,
            final int idle, final int working, final int capacity, final ConsumptionModel model) {
        this.inputBuff = bufferIn;
        this.outputBuff = bufferOut;
        this.fixedECons = idle;
        this.ratedMaxVarECons = working - idle;
        this.processingState = new StationStateImpl.Processing(model);
        this.resourceMovingState = new StationStateImpl.ResourceMoving(model);
        this.currentState = resourceMovingState;
        this.totalConsumption = 0;
        this.processedCount = 0;
        this.lastConsumption = 0;
        this.capacity = capacity;
        this.currentResource = new ArrayList<>();
        this.lastProcessingRate = 0;
    }

    @Override
    public void afterTick(final int t) {
    }

    @Override
    public double getLastStepConsumption() {
        return lastConsumption;
    }

    /*
     * (non-Javadoc)
     * @see domain.workstation.IWorkstation#getProcessedItemsCount()
     */
    @Override
    public int getProcessedItemsCount() {
        return this.processedCount;
    }

    @Override
    public double getTotalConsumption() {
        return this.totalConsumption;
    }

    /*
     * (non-Javadoc)
     * @see
     * simulation.ISimulationComponent#initialize(simulation.ISimulationContext)
     */
    @Override
    public void initialize(final SimulationContext context) {
    }

    /*
     * (non-Javadoc)
     * @see domain.workstation.IWorkstation#isIdle()
     */
    @Override
    public boolean isIdle() {
        return !currentState.isProcessing();
    }

    @VisibleForTesting
    List<Resource> getCurrentResources() {
        return new ArrayList<>(this.currentResource);
    }

    private void pullIn() {
        if (getInputBuffer().getCurrentOccupancyLevel() < getRatedCapacity()) {
            this.addAllResources(getInputBuffer().pullAll());
        } else {
            for (int i = 0; i < getRatedCapacity(); i++) {
                this.addResource(getInputBuffer().pull());
            }
        }
    }

    private void addAllResources(final Collection<Resource> res) {
        this.currentResource.addAll(res);
    }

    private void addResource(final Resource res) {
        this.currentResource.add(res);
    }

    /**
     *
     */
    private void pushOut() {
        final int size = getCurrentResources().size();
        getOutputBuffer().pushAll(getCurrentResources());
        resetCurrentResource();
        incrementProcessedCount(size);
    }

    /*
     * (non-Javadoc)
     * @see simulation.ISimulationComponent#tick()
     */
    @Override
    public void tick(final int t) {
        calculateLastConsumption();
        increaseTotalConsumption(getLastStepConsumption());
        currentState.handleTick(stateContext);
    }

    private void calculateLastConsumption() {
        setLastConsumption(getFixedConsumptionRate() + getCurrentState()
                .getVarConsumptionRate(getRemainingStepsOfResource(),
                        getMaxRemainingStepsOfResource(), stateContext));
    }

    private int getRemainingStepsOfResource() {
        return CollectionUtils.max(currentResource,
                Resource::getCurrentNeededProcessTime);
    }

    private int getMaxRemainingStepsOfResource() {
        return CollectionUtils.max(currentResource,
                Resource::getMaxNeededProcessTime);
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

    private void increaseTotalConsumption(final double consumptionRate) {
        this.totalConsumption = this.totalConsumption + consumptionRate;
    }

    private void incrementProcessedCount(final int size) {
        this.processedCount = this.processedCount + size;

    }

    private void resetCurrentResource() {
        this.currentResource = new ArrayList<>();
    }

    private void setLastConsumption(final double rate) {
        this.lastConsumption = rate;
    }

    /**
     * Returns the current capacity of this workstation.
     *
     * @return the capacity
     */
    @Override
    public final int getRatedCapacity() {
        return capacity;
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    private void setFixedECons(final int fixedECons) {
        this.fixedECons = fixedECons;
    }

    private int getFixedConsumptionRate() {
        return this.fixedECons;
    }

    private int getMaxVarECons() {
        return ratedMaxVarECons;
    }

    @Override
    public final void increaseRatedMaxVarECons(final int shift) {
        this.setRatedMaxVarECons(this.getMaxVarECons() + shift);
    }

    @Override
    public void decreaseRatedMaxVarECons(final int shift) {
        checkArgument(shift < getMaxVarECons(),
                "cant shift more towards speed than available.");
        this.setRatedMaxVarECons(this.getMaxVarECons() - shift);
    }

    @Override
    public void increaseFixedECons(final int shift) {
        this.setFixedECons(this.getFixedConsumptionRate() + shift);
    }

    @Override
    public void decreaseFixedECons(final int shift) {
        checkArgument(shift < getFixedConsumptionRate(),
                "cant shift more towards low consumption than available.");
        this.setFixedECons(this.getFixedConsumptionRate() - shift);
    }

    @Override
    public void acceptVisitor(final WorkstationVisitor subject) {
        subject.register(this);

    }

    @Override
    public void setProcessor(final Processor proc) {
        this.proc = proc;
    }

    /**
     * @param ratedMaxVarECons the ratedMaxVarECons to set
     */
    private void setRatedMaxVarECons(final int ratedMaxVarECons) {
        this.ratedMaxVarECons = ratedMaxVarECons;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(65);
        builder.append("Workstation [fixedECons=").append(fixedECons)
                .append(", ratedMaxVarECons=").append(ratedMaxVarECons)
                .append(", capacity=").append(capacity).append(", hc=")
                .append(this.hashCode()).append("]");
        return builder.toString();
    }

    @Override
    public double getProcessingRate() {
        recalculateProcessingRate();
        return lastProcessingRate;
    }

    private void recalculateProcessingRate() {
        final double cap = getRatedCapacity();
        final double neededProc = (double) getMaxRemainingStepsOfResource() > 0
                ? (double) getMaxRemainingStepsOfResource()
                : Double.POSITIVE_INFINITY;
        setLastProcessingRate(cap / neededProc);
    }

    @Override
    public double getAverageConsumption() {
        return (getMaxVarECons() * getMaxRemainingStepsOfResource()
                + getFixedConsumptionRate())
                / (double) (getMaxRemainingStepsOfResource() + 1);
    }

    private void setLastProcessingRate(final double value) {
        this.lastProcessingRate = value;
    }

    private final class StateContext implements WorkstationContext {

        @Override
        public void processResources(final int steps) {
            for (final Resource r : currentResource) {
                proc.doProcessingStep(r, steps);
            }
        }

        @Override
        public boolean pushConveyer() {
            if (!getCurrentResources().isEmpty()) {
                pushOut();
            }
            if (!getInputBuffer().isEmpty()) {
                pullIn();
                return true;
            }
            return false;
        }

        @Override
        public void setProcessingState() {
            currentState = processingState;

        }

        @Override
        public void setResourceMovingState() {
            currentState = resourceMovingState;
        }

        @Override
        public boolean hasUnfinishedResources() {
            for (final Resource r : currentResource) {
                if (r.needsMoreProcessing()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getRatedVariableConsumption() {
            return ratedMaxVarECons;
        }
    }

    private static final class ProcessorImpl implements Processor {
        @Override
        public void doProcessingStep(final Resource r, final int baseSteps) {
            r.process(baseSteps);
        }
    }
}
