package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.domain.resource.Resource;

/**
 * This class represents the commonalities of the state specific behavior.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
abstract class StationStateImpl implements StationState {
    private final int consumptionRate;

    StationStateImpl(int consumption) {
        this.consumptionRate = consumption;
    }

    @Override
    public int getConsumptionRate() {
        return consumptionRate;
    }

    /**
     * The Class ProcessingState.
     */
    static final class Processing extends StationStateImpl {

        Processing(int consumption) {
            super(consumption);
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#handleTick(domain.IStationContext)
         */
        @Override
        public void handleTick(WorkstationContext context) {
            Resource res = context.getCurrentResource().get();
            res.process(1);
            if (!res.needsMoreProcessing()) {
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
        private boolean idle;

        ResourceMoving(int consumption) {
            super(consumption);
            this.idle = false;
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
            } else {
                this.idle = true;
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
        public int getConsumptionRate() {
            if (idle) {
                return 0;
            }
            return super.getConsumptionRate();
        }
    }
}
