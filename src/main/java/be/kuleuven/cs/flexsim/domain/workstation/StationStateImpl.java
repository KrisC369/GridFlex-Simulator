package be.kuleuven.cs.flexsim.domain.workstation;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This class represents the commonalities of the state specific behavior.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
abstract class StationStateImpl implements StationState {
    private int maxVarConsumptionRate;
    private final ConsumptionModel model;

    StationStateImpl(int varConsumption, ConsumptionModel model) {
        this.maxVarConsumptionRate = varConsumption;
        this.model = model;
    }

    @Override
    public double getVarConsumptionRate(int remainingSteps, int totalSteps) {
        return model.getVarConsumptionRate(remainingSteps, totalSteps,
                maxVarConsumptionRate);
    }

    @Override
    public void setMaxVariableConsumption(int amount) {
        checkArgument(amount >= 0, "Amount %s Can't be negative", amount);
        this.maxVarConsumptionRate = amount;
    }

    @Override
    public int getMaxVariableConsumption() {
        return maxVarConsumptionRate;
    }

    /**
     * The Class ProcessingState.
     */
    static final class Processing extends StationStateImpl {

        Processing(int consumption, ConsumptionModel model) {
            super(consumption, model);
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#handleTick(domain.IStationContext)
         */
        @Override
        public void handleTick(WorkstationContext context) {
            context.processResources(1);
            if (!context.hasUnfinishedResources()) {
                changestate(context);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#isProcessing()
         */
        @Override
        public boolean isProcessing() {
            return true;
        }

        private void changestate(WorkstationContext context) {
            context.setResourceMovingState();
        }

    }

    static final class ResourceMoving extends StationStateImpl {

        ResourceMoving(int consumption, ConsumptionModel model) {
            super(consumption, model);
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#handleTick(domain.IStationContext)
         */
        @Override
        public void handleTick(WorkstationContext context) {
            boolean succesfull = context.pushConveyer();
            if (succesfull) {
                changeState(context);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#isProcessing()
         */
        @Override
        public boolean isProcessing() {
            return false;
        }

        private void changeState(WorkstationContext context) {
            context.setProcessingState();
        }

        @Override
        public void setMaxVariableConsumption(int amount) {
            // noop
        }
    }
}
