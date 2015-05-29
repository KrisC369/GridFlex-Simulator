package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * Different consumption model strategies for calculating energy consumption.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public enum ConsumptionModel {
    /**
     * A Linear consumption model.
     */
    LINEAR {

        @Override
        public double getVarConsumptionRate(int remainingSteps, int maxSteps,
                int maxRate) {
            if (maxSteps <= 0) {
                return 0;
            }
            return (maxRate / (double) maxSteps)
                    * (maxSteps - remainingSteps + 1);
        }

    },

    /**
     * An exponential energy consumption model.
     */
    EXPONENTIAL {

        @Override
        public double getVarConsumptionRate(int remainingSteps, int maxSteps,
                int maxRate) {
            return Math.pow(Math.pow(maxRate, 1 / (double) maxSteps), maxSteps
                    - remainingSteps + 1);
        }

    },

    /**
     * A constant energy consumption model.
     */
    CONSTANT {

        @Override
        public double getVarConsumptionRate(int remainingSteps, int maxSteps,
                int maxRate) {
            return maxRate;
        }

    };

    /**
     * Calculate the instantaneous consumptionRate based on different
     * parameters.
     * 
     * @param remainingSteps
     *            The steps remaining to complete this processing phase.
     * @param maxSteps
     *            the maximum of steps needed to complete a processing phase.
     * @param maxRate
     *            the current maximum rate of consumption.
     * @return a double indicating a consumptionrate factor.
     */
    public abstract double getVarConsumptionRate(int remainingSteps,
            int maxSteps, int maxRate);
}
