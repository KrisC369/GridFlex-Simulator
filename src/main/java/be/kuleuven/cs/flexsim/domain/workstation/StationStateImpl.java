package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * This class represents the commonalities of the state specific behavior.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
abstract class StationStateImpl implements StationState {
    private final ConsumptionModel model;

    StationStateImpl(int varConsumption, ConsumptionModel model) {
        this.model = model;
    }

    @Override
    public ConsumptionModel getModel() {
        return this.model;
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

        @Override
        public double getVarConsumptionRate(int remainingSteps, int totalSteps,
                WorkstationContext context) {
            return getModel().getVarConsumptionRate(remainingSteps, totalSteps,
                    context.getRatedVariableConsumption());
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
        public double getVarConsumptionRate(int remainingSteps, int totalSteps,
                WorkstationContext context) {
            return getModel().getVarConsumptionRate(remainingSteps, totalSteps,
                    0);
        }
    }
}
