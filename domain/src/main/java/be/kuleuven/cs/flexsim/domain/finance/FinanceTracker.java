package be.kuleuven.cs.flexsim.domain.finance;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;

/**
 * Represents an entity for tracking financing data about trackable instances.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface FinanceTracker extends SimulationComponent {

    /**
     * Returns the total reward metric value for this tracker.
     *
     * @return the reward.
     */
    double getTotalReward();

    /**
     * Returns the total cost metric for this tracker.
     *
     * @return the totalCost.
     */
    double getTotalCost();

    /**
     * Returns the total profit as the reward minus the cost.
     *
     * @return the total reward minus the total cost.
     */
    double getTotalProfit();

}