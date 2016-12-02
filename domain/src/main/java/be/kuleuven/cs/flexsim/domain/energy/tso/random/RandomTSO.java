package be.kuleuven.cs.flexsim.domain.energy.tso.random;

import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;
import be.kuleuven.cs.flexsim.domain.util.listener.MultiplexListener;
import be.kuleuven.cs.flexsim.domain.util.listener.NoopListener;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.List;

/**
 * Represents a TSO implementation with random signal.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class RandomTSO implements BalancingSignal, SimulationComponent {
    private final RandomGenerator g;
    private final int min;
    private final int max;
    private int currentValue;
    private Listener<? super Integer> newBalanceValueListener;

    /**
     * Default constructor.
     *
     * @param min
     *            the minimum value for the random generator.
     * @param max
     *            the maximum value for the random generator.
     * @param g
     *            The random generator to use.
     */
    public RandomTSO(final int min, final int max, final RandomGenerator g) {
        this.min = min;
        this.max = max;
        this.g = g;
        currentValue = 0;
        this.newBalanceValueListener = NoopListener.INSTANCE;
    }

    /**
     * @return the generator
     */
    final RandomGenerator getGenerator() {
        return g;
    }

    /**
     * @return the min
     */
    final int getMin() {
        return min;
    }

    /**
     * @return the max
     */
    final int getMax() {
        return max;
    }

    @Override
    public int getCurrentImbalance() {
        return currentValue;
    }

    @Override
    public void addNewBalanceValueListener(final Listener<? super Integer> listener) {
        this.newBalanceValueListener = MultiplexListener
                .plus(this.newBalanceValueListener, listener);
    }

    @Override
    public void initialize(final SimulationContext context) {
    }

    @Override
    public void afterTick(final int t) {
        this.currentValue = getGenerator()
                .nextInt(Math.abs(getMax() - getMin())) + getMin();
    }

    @Override
    public void tick(final int t) {
        newBalanceValueListener.eventOccurred(currentValue);
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }
}
