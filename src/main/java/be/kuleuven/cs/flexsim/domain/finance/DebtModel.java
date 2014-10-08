package be.kuleuven.cs.flexsim.domain.finance;

/**
 * The debt or payment model representation.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
public enum DebtModel {
    /**
     * A constant debtmodel, using a constant pricerate for the consumption.
     */
    CONSTANT {

        @Override
        public double calculateDebt(int timestep, double lastConsumption) {
            return lastConsumption;
        }

    };

    /**
     * Calculates the payment price for a specified consumption amount for a
     * given timestep.
     * 
     * @param timestep
     *            The timestep.
     * @param lastConsumption
     *            The consumption amount.
     * @return the due payment amount.
     */
    public abstract double calculateDebt(int timestep, double lastConsumption);

}
