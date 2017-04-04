package be.kuleuven.cs.gridflex.domain.finance;

import be.kuleuven.cs.gridflex.domain.process.ResourceConsumptionTrackableComponent;
import be.kuleuven.cs.gridflex.domain.resource.Resource;
import be.kuleuven.cs.gridflex.domain.site.Site;
import be.kuleuven.cs.gridflex.event.Event;
import be.kuleuven.cs.gridflex.simulation.SimulationComponent;
import be.kuleuven.cs.gridflex.simulation.SimulationContext;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks and finalizes the finances of productionlines.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class FinanceTrackerImpl implements FinanceTracker {

    private final ResourceConsumptionTrackableComponent target;
    private Optional<SimulationContext> context;
    private double totalReward;
    private double totalCost;
    private final RewardModel rewardMod;
    private final DebtModel debtMod;
    private long itemCount;
    private long uid;

    /**
     * Default constructor based on trackable components.
     *
     * @param target the target component to track.
     * @param rm     The rewardModel to use.
     * @param dm     The debtModel to use.
     */
    protected FinanceTrackerImpl(final ResourceConsumptionTrackableComponent target,
            final RewardModel rm, final DebtModel dm) {
        this.target = target;
        this.context = Optional.absent();
        this.rewardMod = rm;
        this.debtMod = dm;
        this.itemCount = 0;
    }

    @Override
    public void initialize(final SimulationContext context) {
        this.context = Optional.of(context);
        this.uid = context.getUIDGenerator().getNextUID();
    }

    /**
     * This method refines the following documentation by generating a report
     * event when there is simulation context present for this line instance.
     * {@inheritDoc}
     */
    @Override
    public void afterTick(final int t) {
        calculateCost(t);
        calculateReward(t);
        report();
    }

    private void calculateCost(final int t) {
        incrementTotalCost(
                debtMod.calculateDebt(t, getTarget().getLastStepConsumption()));
    }

    private void calculateReward(final int t) {
        int rewardIncrement = 0;
        for (final Resource r : getTarget().takeResources()) {
            rewardIncrement += rewardMod.calculateReward(t, r);
            incrementItemCount();
        }
        increaseTotalReward(rewardIncrement);
    }

    private void incrementItemCount() {
        this.itemCount++;
    }

    private long getItemCount() {
        return this.itemCount;
    }

    private void report() {
        publishReport(getTarget().getLastStepConsumption(),
                getTarget().getTotalConsumption(),
                getTarget().getBufferOccupancyLevels(), getTotalProfit());
    }

    private void publishReport(final double totalLaststep, final double totalTotal,
            final List<Integer> buffSizes, final double profit) {
        if (!this.context.isPresent()) {
            throw new IllegalStateException(
                    "This component has not been correctly configured with a context.");
        }
        final Event e = getContext().getEventFactory().build("report");
        e.setAttribute("pLinehash", this.hashCode());
        e.setAttribute("time",
                getContext().getSimulationClock().getTimeCount());
        e.setAttribute(uid + "totalLaststepE", totalLaststep);
        e.setAttribute(uid + "totalTotalE", totalTotal);
        e.setAttribute(uid + "totalProfitM", profit);
        int idx = 0;
        for (final long i : buffSizes) {
            e.setAttribute(uid + "_buffer_" + idx++, i);
        }
        e.setAttribute(uid + "buffer_Fin", getItemCount());
        getContext().getEventbus().post(e);

    }

    @Override
    public void tick(final int t) {
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        final List<SimulationComponent> toret = new ArrayList<>();
        toret.add(getTarget());
        return toret;
    }

    /**
     * @return the target
     */
    private ResourceConsumptionTrackableComponent getTarget() {
        return target;
    }

    /**
     * @return the context
     */
    private SimulationContext getContext() {
        return context.get();
    }

    /*
     * (non-Javadoc)
     * @see
     * be.kuleuven.cs.gridflex.domain.finance.FinanceTracker#getTotalReward()
     */
    @Override
    public double getTotalReward() {
        return this.totalReward;
    }

    protected final void increaseTotalReward(final int increment) {
        this.totalReward = this.totalReward + increment;
    }

    /*
     * (non-Javadoc)
     * @see be.kuleuven.cs.gridflex.domain.finance.FinanceTracker#getTotalCost()
     */
    @Override
    public double getTotalCost() {
        return totalCost;
    }

    protected final void incrementTotalCost(final double incr) {
        this.totalCost = this.totalCost + incr;
    }

    /*
     * (non-Javadoc)
     * @see
     * be.kuleuven.cs.gridflex.domain.finance.FinanceTracker#getTotalProfit()
     */
    @Override
    public double getTotalProfit() {
        return getTotalReward() - getTotalCost();
    }

    /**
     * Factory method for creating finance tracker object with default reward
     * and debt models.
     *
     * @param target The trackable target to inspect.
     * @return A fully instantiated FinanceTracker object.
     */
    public static FinanceTrackerImpl createDefault(
            final ResourceConsumptionTrackableComponent target) {
        return new FinanceTrackerImpl(target, RewardModel.CONSTANT,
                DebtModel.CONSTANT);

    }

    /**
     * Factory method for creating finance tracker object with custom reward and
     * debt models.
     *
     * @param target The trackable target to inspect.
     * @param rm     The reward model to use.
     * @param dm     The debt model to use.
     * @return A fully instantiated FinanceTracker object.
     */
    public static FinanceTrackerImpl createCustom(
            final ResourceConsumptionTrackableComponent target, final RewardModel rm,
            final DebtModel dm) {
        return new FinanceTrackerImpl(target, rm, dm);
    }

    /**
     * Aggregates multiple finance tracker instances together and sums values.
     *
     * @param tt the targets.
     * @return a FinanceTracker instance.
     */
    public static FinanceTracker createAggregate(final FinanceTracker... tt) {
        return new FinanceAggregatingDecorator(tt);
    }

    /**
     * Create a new finance tracker with activation-rewards.
     *
     * @param target the target.
     * @param reward the reward for an activation.
     * @return a FinanceTracker instance.
     */
    public static FinanceTracker createBalancingFeeTracker(final Site target,
            final int reward) {
        return new BalancingFeeTracker(target, reward);
    }

    /**
     * Create a new finance tracker with activation-rewards.
     *
     * @param target The target.
     * @param reward The reward for an activation.
     * @param factor The retribution factor.
     * @return a FinanceTracker instance.
     */
    public static FinanceTracker createCustomBalancingFeeTracker(final Site target,
            final int reward, final double factor) {
        return new BalancingFeeTracker(target, reward, factor);
    }
}
