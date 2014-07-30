package be.kuleuven.cs.flexsim.domain.finance;

/**
 * Represents an entity for tracking financing data about trackable instances.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public interface FinanceTracker {

    /**
     * Returns the total reward metric value for this tracker.
     * 
     * @return the reward.
     */
    public abstract int getTotalReward();

    /**
     * Returns the total cost metric for this tracker.
     * 
     * @return the totalCost.
     */
    public abstract int getTotalCost();

    /**
     * Returns the total profit as the reward minus the cost.
     * 
     * @return the total reward minus the total cost.
     */
    public abstract int getTotalProfit();

}