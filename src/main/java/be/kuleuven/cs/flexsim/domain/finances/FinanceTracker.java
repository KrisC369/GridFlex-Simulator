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
public class FinanceTracker implements SimulationComponent {

    private final ProcessTrackableSimulationComponent target;
    private Optional<SimulationContext> context;
    private int totalReward;
    private int totalCost;
    private RewardModel rewardMod;
    private DebtModel debtMod;

    /**
     * Default constructor based on trackable components.
     * 
     * @param target
     *            the target component to track.
     */
    public FinanceTracker(ProcessTrackableSimulationComponent target,
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

    public int getTotalReward() {
        return this.totalReward;
    }

    private void increaseTotalReward(int increment) {
        this.totalReward = this.totalReward + increment;
    }

    /**
     * @return the totalCost
     */
    public final int getTotalCost() {
        return totalCost;
    }

    private final void incrementTotalCost(int incr) {
        this.totalCost = this.totalCost + incr;
    }

}
