package be.kuleuven.cs.flexsim.domain.aggregation.brp;

import static com.google.common.base.Preconditions.checkArgument;
import be.kuleuven.cs.flexsim.domain.finance.DebtModel;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTrackerImpl;
import be.kuleuven.cs.flexsim.domain.finance.RewardModel;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;

/**
 * This class mediates payments and fees for sites as a result of working with
 * an aggregator.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class RenumerationMediator extends FinanceTrackerImpl {
    private SiteFlexAPI target;
    private long currentBudget;

    private RenumerationMediator(Site client, double reservePortion,
            double activationPortion) {
        super(client, RewardModel.NONE, DebtModel.NONE);
        this.target = client;
        this.currentBudget = 0;
    }

    /**
     * Return this mediator's target.
     *
     * @return the api target.
     */
    public SiteFlexAPI getTarget() {
        return this.target;
    }

    /**
     * Factory method for a RenumerationMediator.
     *
     * @param client
     *            The target client.
     * @param reservePortion
     *            The portion of the budget to use for reservation payments.
     * @param activationPortion
     *            The portion of the budget to use for activation payments.
     * @return The newly created RenumerationMediator object.
     */
    public static RenumerationMediator create(Site client,
            double reservePortion, double activationPortion) {
        return new RenumerationMediator(client, reservePortion,
                activationPortion);
    }

    /**
     * Set the current budget to a new value.
     *
     * @param budget
     *            The new budget. Should not be negative.
     */
    public void setBudget(long budget) {
        checkArgument(budget >= 0, "Budget should not be negative.");
        this.currentBudget = budget;
    }

    long getCurrentBudget() {
        return this.currentBudget;
    }
}
