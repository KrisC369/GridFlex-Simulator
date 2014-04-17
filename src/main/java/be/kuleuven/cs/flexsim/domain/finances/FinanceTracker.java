package be.kuleuven.cs.flexsim.domain.finances;

import java.util.ArrayList;
import java.util.List;

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
public final class FinanceTracker implements SimulationComponent {

    private final ProcessTrackableSimulationComponent target;
    private Optional<SimulationContext> context;
    private int totalReward;
    private int totalCost;
    private final RewardModel rewardMod;
    private final DebtModel debtMod;

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
    private FinanceTracker(ProcessTrackableSimulationComponent target,
            RewardModel rm, DebtModel dm) {
        this.target = target;
        this.context = Optional.absent();
        this.rewardMod = rm;
        this.debtMod = dm;
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
        calculateConsumption(t);
        calculateReward(t);
        report();
    }

    private void calculateConsumption(int t) {
        incrementTotalCost(debtMod.calculateDebt(t, getTarget()
                .getAggregatedLastStepConsumptions()));
    }

    private void calculateReward(int t) {
        int rewardIncrement = 0;
        for (Resource r : getTarget().takeResources()) {
            rewardIncrement += rewardMod.calculateReward(t, r);
        }
        increaseTotalReward(rewardIncrement);
    }

    private void report() {
        publishReport(getTarget().getAggregatedLastStepConsumptions(),
                getTarget().getAggregatedTotalConsumptions(), getTarget()
                        .getBufferOccupancyLevels());
    }

    private void publishReport(int totalLaststep, int totalTotal,
            List<Integer> buffSizes) {
        if (this.context.isPresent()) {
            Event e = getContext().getEventFactory().build("report");
            e.setAttribute("pLinehash", this.hashCode());
            e.setAttribute("time", getContext().getSimulationClock()
                    .getTimeCount());
            e.setAttribute("totalLaststepE", totalLaststep);
            e.setAttribute("totalTotalE", totalTotal);
            int idx = 0;
            for (long i : buffSizes) {
                e.setAttribute("buffer_" + idx++, i);
            }
            getContext().getEventbus().post(e);
        }
    }

    @Override
    public void tick(int t) {
        // TODO Auto-generated method stub

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
    private ProcessTrackableSimulationComponent getTarget() {
        return target;
    }

    /**
     * @return the context
     */
    private SimulationContext getContext() {
        return context.get();
    }

    /**
     * Returns the total reward metric value for this tracker.
     * 
     * @return the reward.
     */
    public int getTotalReward() {
        return this.totalReward;
    }

    private void increaseTotalReward(int increment) {
        this.totalReward = this.totalReward + increment;
    }

    /**
     * Returns the total cost metric for this tracker.
     * 
     * @return the totalCost.
     */
    public int getTotalCost() {
        return totalCost;
    }

    private void incrementTotalCost(int incr) {
        this.totalCost = this.totalCost + incr;
    }

    /**
     * Returns the total profit as the reward minus the cost.
     * 
     * @return the total reward minus the total cost.
     */
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
    public static FinanceTracker createDefault(
            ProcessTrackableSimulationComponent target) {
        return new FinanceTracker(target, RewardModel.CONSTANT,
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
    public static FinanceTracker createCustom(
            ProcessTrackableSimulationComponent target, RewardModel rm,
            DebtModel dm) {
        return new FinanceTracker(target, rm, dm);
    }
}
