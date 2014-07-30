package be.kuleuven.cs.flexsim.domain.finance;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Adapter class for aggregating and summing multiple trackers.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
class FinanceAggregatingDecorator implements FinanceTracker {

    private final List<FinanceTracker> targets;

    FinanceAggregatingDecorator(FinanceTracker... tt) {
        this.targets = Lists.newArrayList(tt);
    }

    @Override
    public int getTotalReward() {
        int sum = 0;
        for (FinanceTracker t : targets) {
            sum += t.getTotalReward();
        }
        return sum;
    }

    @Override
    public int getTotalCost() {
        int sum = 0;
        for (FinanceTracker t : targets) {
            sum += t.getTotalCost();
        }
        return sum;
    }

    @Override
    public int getTotalProfit() {
        int sum = 0;
        for (FinanceTracker t : targets) {
            sum += t.getTotalProfit();
        }
        return sum;
    }
}
