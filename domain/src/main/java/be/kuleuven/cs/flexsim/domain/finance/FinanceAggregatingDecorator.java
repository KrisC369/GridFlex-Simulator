package be.kuleuven.cs.flexsim.domain.finance;

import java.util.List;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * Adapter class for aggregating and summing multiple trackers.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
class FinanceAggregatingDecorator implements FinanceTracker {

    private final List<FinanceTracker> targets;

    FinanceAggregatingDecorator(FinanceTracker... tt) {
        this.targets = Lists.newArrayList(tt);
    }

    @Override
    public double getTotalReward() {
        int sum = 0;
        for (FinanceTracker t : targets) {
            sum += t.getTotalReward();
        }
        return sum;
    }

    @Override
    public double getTotalCost() {
        int sum = 0;
        for (FinanceTracker t : targets) {
            sum += t.getTotalCost();
        }
        return sum;
    }

    @Override
    public double getTotalProfit() {
        int sum = 0;
        for (FinanceTracker t : targets) {
            sum += t.getTotalProfit();
        }
        return sum;
    }

    @Override
    public void afterTick(int t) {
        for (FinanceTracker tr : targets) {
            tr.afterTick(t);
        }

    }

    @Override
    public void tick(int t) {
        for (FinanceTracker tr : targets) {
            tr.tick(t);
        }
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        List<SimulationComponent> subcomp = Lists.newArrayList();
        for (FinanceTracker tr : targets) {
            subcomp.addAll(tr.getSimulationSubComponents());
        }
        return subcomp;
    }

    @Override
    public void initialize(SimulationContext context) {
        for (FinanceTracker tr : targets) {
            tr.initialize(context);
        }
    }
}
