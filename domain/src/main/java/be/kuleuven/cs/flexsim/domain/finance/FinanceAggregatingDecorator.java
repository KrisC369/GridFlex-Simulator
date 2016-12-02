package be.kuleuven.cs.flexsim.domain.finance;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Adapter class for aggregating and summing multiple trackers.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
class FinanceAggregatingDecorator implements FinanceTracker {

    private final List<FinanceTracker> targets;

    FinanceAggregatingDecorator(final FinanceTracker... tt) {
        this.targets = Lists.newArrayList(tt);
    }

    @Override
    public double getTotalReward() {
        int sum = 0;
        for (final FinanceTracker t : targets) {
            sum += t.getTotalReward();
        }
        return sum;
    }

    @Override
    public double getTotalCost() {
        int sum = 0;
        for (final FinanceTracker t : targets) {
            sum += t.getTotalCost();
        }
        return sum;
    }

    @Override
    public double getTotalProfit() {
        int sum = 0;
        for (final FinanceTracker t : targets) {
            sum += t.getTotalProfit();
        }
        return sum;
    }

    @Override
    public void afterTick(final int t) {
        for (final FinanceTracker tr : targets) {
            tr.afterTick(t);
        }

    }

    @Override
    public void tick(final int t) {
        for (final FinanceTracker tr : targets) {
            tr.tick(t);
        }
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        final List<SimulationComponent> subcomp = Lists.newArrayList();
        for (final FinanceTracker tr : targets) {
            subcomp.addAll(tr.getSimulationSubComponents());
        }
        return subcomp;
    }

    @Override
    public void initialize(final SimulationContext context) {
        for (final FinanceTracker tr : targets) {
            tr.initialize(context);
        }
    }
}
