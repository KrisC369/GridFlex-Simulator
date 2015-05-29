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
public final class RenumerationMediator extends FinanceTrackerImpl {
    private final SiteFlexAPI target;
    private long currentReservationBudget;
    private long currentActivationBudget;
    private final double reservationPortion;
    private final double activationPortion;

    private RenumerationMediator(Site client, double reservePortion) {
        super(client, RewardModel.NONE, DebtModel.NONE);
        this.target = client;
        this.currentReservationBudget = 0;
        this.currentActivationBudget = 0;
        this.reservationPortion = reservePortion;
        this.activationPortion = 1 - reservePortion;
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
     * @return The newly created RenumerationMediator object.
     */
    public static RenumerationMediator create(Site client, double reservePortion) {
        return new RenumerationMediator(client, reservePortion);
    }

    /**
     * Set the current budget to a new value.
     *
     * @param budget
     *            The new budget. Should not be negative.
     */
    public void setBudget(long budget) {
        checkArgument(budget >= 0, "Budget should not be negative.");
        this.currentActivationBudget = (long) (budget * getActivationPortion());
        this.currentReservationBudget = (long) (budget * getReservationPortion());

    }

    private double getActivationPortion() {
        return this.activationPortion;
    }

    private double getReservationPortion() {
        return this.reservationPortion;
    }

    private long getActivationBudget() {
        return this.currentActivationBudget;
    }

    private long getReservationBudget() {
        return this.currentReservationBudget;
    }

    long getCurrentBudget() {
        return getActivationBudget() + getReservationBudget();
    }

    /**
     * Register a payment for reservation by specifying the portion of the
     * reservation budget should be awarded.
     *
     * @param proportion
     *            The proportion ( a double between 0 and 1 inclusive) of the
     *            budget to award.
     */
    public void registerReservation(double proportion) {
        checkArgument(proportion >= 0 && proportion <= 1,
                "Proportions should be between 0 and 1");

        increaseTotalReward((int) (getReservationBudget() * proportion));
    }

    /**
     * Register a payment for activation by specifying the portion of the
     * activation budget should be awarded.
     *
     * @param proportion
     *            The proportion ( a double between 0 and 1 inclusive) of the
     *            budget to award.
     */
    public void registerActivation(double proportion) {
        checkArgument(proportion >= 0 && proportion <= 1,
                "Proportions should be between 0 and 1");
        increaseTotalReward((int) (getActivationBudget() * proportion));
    }
}
