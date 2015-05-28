package be.kuleuven.cs.flexsim.domain.aggregation.independent;

import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregationStrategy;
import be.kuleuven.cs.flexsim.domain.aggregation.AggregationStrategyImpl;
import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * Represents an energy aggregator implementation.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class IndependentAggregator extends Aggregator {

    private int tickcount;
    private final int aggFreq;
    private final BalancingSignal tso;

    /**
     * Constructor with custom aggregation strategy.
     *
     * @param tso
     *            the tso to take steering signals from.
     * @param frequency
     *            the frequency with which to perform aggregation functions.
     * @param strategy
     *            The aggregation strategy to use.
     */
    public IndependentAggregator(BalancingSignal tso, int frequency,
            AggregationStrategy strategy) {
        super(strategy);
        this.tso = tso;
        this.tickcount = 1;
        this.aggFreq = frequency;
    }

    /**
     * Default constructor with default aggregation strategy: Cartesianproduct.
     *
     * @param tso
     *            the tso to take steering signals from.
     * @param frequency
     *            the frequency with which to perform aggregation functions.
     */
    public IndependentAggregator(BalancingSignal tso, int frequency) {
        this(tso, frequency, AggregationStrategyImpl.CARTESIANPRODUCT);
    }

    protected final int getTargetFlex() {
        return getTso().getCurrentImbalance() * 1;
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public void afterTick(int t) {
    }

    @Override
    public void tick(int t) {
        if (tickcount++ % aggFreq == 0) {
            doAggregationStep(t, getTargetFlex(), gatherFlexInfo());
        }
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    /**
     * @return the tso.
     */
    final BalancingSignal getTso() {
        return tso;
    }
}