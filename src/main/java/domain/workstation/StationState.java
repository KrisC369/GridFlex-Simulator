package domain.workstation;

import domain.resource.IResource;

/**
 * This class represents the commonalities of the state specific behavior.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
abstract class StationState implements IStationState {
    /**
     * The Class ProcessingState.
     */
    static final class Processing extends StationState {

        Processing(int consumption) {
            super(consumption);
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#handleTick(domain.IStationContext)
         */
        @Override
        public void handleTick(IStationContext context) {
            IResource res = context.getCurrentResource().get();
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

        private void changestate(IStationContext context) {
            context.setResourceMovingState();
        }

    }

    static final class ResourceMoving extends StationState {

        ResourceMoving(int consumption) {
            super(consumption);
        }

        /*
         * (non-Javadoc)
         * 
         * @see domain.IStationState#handleTick(domain.IStationContext)
         */
        @Override
        public void handleTick(IStationContext context) {
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

        private void changeState(IStationContext context) {
            context.setProcessingState();
        }

    }

    private final int consumptionRate;

    StationState(int consumption) {
        this.consumptionRate = consumption;
    }

    @Override
    public int getConsumptionRate() {
        return consumptionRate;
    }

}
