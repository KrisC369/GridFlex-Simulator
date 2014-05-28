package be.kuleuven.cs.flexsim.domain.workstation;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.util.CollectionUtils;
import be.kuleuven.cs.flexsim.domain.util.IntNNFunction;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.annotations.VisibleForTesting;

/**
 * Main workstation class representing machines that perform work and consume
 * energy.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class WorkstationImpl implements Workstation {

    private static final IntNNFunction<Resource> CURRENT_REMAINING_STEPS = new IntNNFunction<Resource>() {
        @Override
        public int apply(Resource input) {
            return input.getCurrentNeededProcessTime();
        }
    };

    private static final IntNNFunction<Resource> MAX_REMAINING_STEPS = new IntNNFunction<Resource>() {
        @Override
        public int apply(Resource input) {
            return input.getMaxNeededProcessTime();
        }
    };

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
    private final int capacity;
    private int speedfactor;
    private final WorkstationContext stateContext = new WorkstationContext() {

        @Override
        public void processResources(int steps) {
            for (Resource r : currentResource) {
                r.process(steps);
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
            setLastConsumption(0);
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
            for (Resource r : currentResource) {
                if (r.needsMoreProcessing()) {
                    return true;
                }
            }
            return false;
        }
    };

    /**
     * Constructor that creates a workstation instance from an in and an out
     * buffer.
     * 
     * @param bufferIn
     *            The In buffer.
     * @param bufferOut
     *            The Out buffer.
     * @param linear
     */
    @VisibleForTesting
    WorkstationImpl(Buffer<Resource> bufferIn, Buffer<Resource> bufferOut,
            int idle, int working, int capacity, ConsumptionModel model) {
        this.inputBuff = bufferIn;
        this.outputBuff = bufferOut;
        this.fixedECons = idle;
        this.processingState = new StationStateImpl.Processing(working - idle,
                model);
        this.resourceMovingState = new StationStateImpl.ResourceMoving(0, model);
        this.currentState = resourceMovingState;
        this.totalConsumption = 0;
        this.processedCount = 0;
        this.lastConsumption = 0;
        this.capacity = capacity;
        this.currentResource = new ArrayList<>();
        this.speedfactor = 0;
    }

    @Override
    public void afterTick(int t) {
    }

    @Override
    public double getLastStepConsumption() {
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
    public double getTotalConsumption() {
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

    @VisibleForTesting
    List<Resource> getCurrentResources() {
        return new ArrayList<>(this.currentResource);
    }

    private void pullIn() {
        if (getInputBuffer().getCurrentOccupancyLevel() < getCapacity()) {
            this.addAllResources(getInputBuffer().pullAll());
        } else {
            for (int i = 0; i < getCapacity(); i++) {
                this.addResource(getInputBuffer().pull());
            }
        }
    }

    private void addAllResources(Collection<Resource> res) {
        this.currentResource.addAll(res);
    }

    private void addResource(Resource res) {
        this.currentResource.add(res);
    }

    /**
     * 
     */
    private void pushOut() {
        int size = getCurrentResources().size();
        getOutputBuffer().pushAll(getCurrentResources());
        resetCurrentResource();
        incrementProcessedCount(size);
    }

    /*
     * (non-Javadoc)
     * 
     * @see simulation.ISimulationComponent#tick()
     */
    @Override
    public void tick(int t) {
        setLastConsumption(getFixedConsumptionRate()
                + getCurrentState().getVarConsumptionRate(getRemainingSteps(),
                        getRemainingMaxSteps()));
        increaseTotalConsumption(getLastStepConsumption());
        currentState.handleTick(stateContext);
    }

    private int getRemainingSteps() {
        return CollectionUtils.max(currentResource, CURRENT_REMAINING_STEPS);
    }

    private int getRemainingMaxSteps() {
        return CollectionUtils.max(currentResource, MAX_REMAINING_STEPS);
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

    private void increaseTotalConsumption(double consumptionRate) {
        this.totalConsumption = this.totalConsumption + consumptionRate;
    }

    private void incrementProcessedCount(int size) {
        this.processedCount = this.processedCount + size;

    }

    private void resetCurrentResource() {
        this.currentResource = new ArrayList<>();
    }

    private void setLastConsumption(double rate) {
        this.lastConsumption = rate;
    }

    /**
     * Returns the current capacity of this workstation.
     * 
     * @return the capacity
     */
    public final int getCapacity() {
        return capacity;
    }

    private int getFixedConsumptionRate() {
        return fixedECons;
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    @Override
    public void setSpeedVsEConsumptionRatio(int consumptionShift,
            int speedShift, boolean favorSpeed) {
        if (favorSpeed) {
            checkArgument(consumptionShift < getMaxVarECons(),
                    "cant shift more towards speed than available.");
            setFixedECons(getFixedECons() + consumptionShift);
            setMaxVarECons(getMaxVarECons() - consumptionShift);
            setProcessingSpeed(getProcessingSpeed() + speedShift);
        } else {
            checkArgument(consumptionShift < getFixedECons(),
                    "cant shift more towards low consumption than available.");
            setFixedECons(getFixedECons() - consumptionShift);
            setMaxVarECons(getMaxVarECons() + consumptionShift);
            setProcessingSpeed(getProcessingSpeed() - speedShift);
        }
    }

    private void setProcessingSpeed(int i) {
        this.speedfactor = i;
    }

    private int getProcessingSpeed() {
        return speedfactor;
    }

    private final void setFixedECons(int fixedECons) {
        this.fixedECons = fixedECons;
    }

    private final void setMaxVarECons(int shift) {
        getCurrentState().setMaxVariableConsumption(shift);
    }

    private int getFixedECons() {
        return this.fixedECons;
    }

    private int getMaxVarECons() {
        return getCurrentState().getMaxVariableConsumption();
    }
}
