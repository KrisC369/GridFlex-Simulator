package be.kuleuven.cs.flexsim.domain.energy.generation;

import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * Represents a TSO implementation with random signal.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class RandomOutputGenerator implements EnergyProductionTrackable {
    private final RandomGenerator g;
    private final int min;
    private final int max;
    private int currentValue;
    private long totalProduction;

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
    public RandomOutputGenerator(int min, int max, RandomGenerator g) {
        this.min = min;
        this.max = max;
        this.g = g;
        this.currentValue = 0;
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
    public double getLastStepProduction() {
        return currentValue;
    }

    @Override
    public double getTotalProduction() {
        return totalProduction;
    }

    @Override
    public void afterTick(int t) {
        updateCurrentValue();
    }

    private void updateCurrentValue() {
        this.currentValue = getGenerator().nextInt(
                Math.abs(getMax() - getMin()))
                + getMin();
        this.totalProduction += this.currentValue;
    }

    @Override
    public void tick(int t) {
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    @Override
    public void initialize(SimulationContext context) {
    }
}
