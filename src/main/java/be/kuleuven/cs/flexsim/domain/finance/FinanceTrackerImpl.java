package be.kuleuven.cs.flexsim.domain.finance;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.process.ResourceConsumptionTrackableComponent;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.base.Optional;

/**
 * Tracks and finalizes the finances of productionlines.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public final class FinanceTrackerImpl implements SimulationComponent,
        FinanceTracker {

    private final ResourceConsumptionTrackableComponent target;
    private Optional<SimulationContext> context;
    private int totalReward;
    private int totalCost;
    private final RewardModel rewardMod;
    private final DebtModel debtMod;
    private long itemCount;

    /**
     * Default constructor based on trackable components.
     * 
     * @param target
     *            the target component to track.
     * @param rm
     *            The rewardModel to use.
     * @param dm
     *            The debtModel to use.
     */
    private FinanceTrackerImpl(ResourceConsumptionTrackableComponent target,
            RewardModel rm, DebtModel dm) {
        this.target = target;
        this.context = Optional.absent();
        this.rewardMod = rm;
        this.debtMod = dm;
        this.itemCount = 0;
    }

    @Override
    public void initialize(SimulationContext context) {
        this.context = Optional.of(context);
    }

    /**
     * This method refines the following documentation by generating a report
     * event when there is simulation context present for this line instance.
     * {@inheritDoc}
     */
    @Override
    public void afterTick(int t) {
        calculateCost(t);
        calculateReward(t);
        report();
    }

    private void calculateCost(int t) {
        incrementTotalCost(debtMod.calculateDebt(t, getTarget()
                .getAggregatedLastStepConsumptions()));
    }

    private void calculateReward(int t) {
        int rewardIncrement = 0;
        for (Resource r : getTarget().takeResources()) {
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
        publishReport(getTarget().getAggregatedLastStepConsumptions(),
                getTarget().getAggregatedTotalConsumptions(), getTarget()
                        .getBufferOccupancyLevels(), getTotalProfit());
    }

    private void publishReport(int totalLaststep, int totalTotal,
            List<Integer> buffSizes, int profit) {
        if (!this.context.isPresent()) {
            throw new IllegalStateException(
                    "This component has not been correctly configured with a context.");
        }
        Event e = getContext().getEventFactory().build("report");
        e.setAttribute("pLinehash", this.hashCode());
        e.setAttribute("time", getContext().getSimulationClock().getTimeCount());
        e.setAttribute("totalLaststepE", totalLaststep);
        e.setAttribute("totalTotalE", totalTotal);
        e.setAttribute("totalProfitM", profit);
        int idx = 0;
        for (long i : buffSizes) {
            e.setAttribute("buffer_" + idx++, i);
        }
        e.setAttribute("buffer_Fin", getItemCount());
        getContext().getEventbus().post(e);

    }

    @Override
    public void tick(int t) {
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        List<SimulationComponent> toret = new ArrayList<SimulationComponent>();
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
     * 
     * @see
     * be.kuleuven.cs.flexsim.domain.finance.FinanceTracker#getTotalReward()
     */
    @Override
    public int getTotalReward() {
        return this.totalReward;
    }

    private void increaseTotalReward(int increment) {
        this.totalReward = this.totalReward + increment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see be.kuleuven.cs.flexsim.domain.finance.FinanceTracker#getTotalCost()
     */
    @Override
    public int getTotalCost() {
        return totalCost;
    }

    private void incrementTotalCost(int incr) {
        this.totalCost = this.totalCost + incr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * be.kuleuven.cs.flexsim.domain.finance.FinanceTracker#getTotalProfit()
     */
    @Override
    public int getTotalProfit() {
        return getTotalReward() - getTotalCost();
    }

    /**
     * Factory method for creating finance tracker object with default reward
     * and debt models.
     * 
     * @param target
     *            The trackable target to inspect.
     * @return A fully instantiated FinanceTracker object.
     */
    public static FinanceTrackerImpl createDefault(
            ResourceConsumptionTrackableComponent target) {
        return new FinanceTrackerImpl(target, RewardModel.CONSTANT,
                DebtModel.CONSTANT);

    }

    /**
     * Factory method for creating finance tracker object with custom reward and
     * debt models.
     * 
     * @param target
     *            The trackable target to inspect.
     * @param rm
     *            The reward model to use.
     * @param dm
     *            The debt model to use.
     * @return A fully instantiated FinanceTracker object.
     */
    public static FinanceTrackerImpl createCustom(
            ResourceConsumptionTrackableComponent target, RewardModel rm,
            DebtModel dm) {
        return new FinanceTrackerImpl(target, rm, dm);
    }

    /**
     * Aggregates multiple finance tracker instances together and sums values.
     * 
     * @param tt
     *            the targets.
     * @return a FinanceTracker instance.
     */
    public static FinanceTracker createAggregate(FinanceTracker... tt) {
        return new FinanceAggregatingDecorator(tt);
    }
}
