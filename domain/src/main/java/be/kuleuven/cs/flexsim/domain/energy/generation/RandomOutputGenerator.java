package be.kuleuven.cs.flexsim.domain.energy.generation;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a TSO implementation with random signal.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class RandomOutputGenerator implements EnergyProductionTrackable {
    private RandomGenerator g;
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
    @VisibleForTesting
    RandomOutputGenerator(final int min, final int max, final RandomGenerator g) {
        checkArgument(min <= 0, "Minimum value should be less or equal than 0");
        checkArgument(max >= 0, "Minimum value should be greater than 0");
        this.min = min;
        this.max = max;
        this.g = g;
        this.currentValue = 0;
    }

    /**
     * Default constructor.
     * 
     * @param min
     *            the minimum value for the random generator.
     * @param max
     *            the maximum value for the random generator.
     */
    public RandomOutputGenerator(final int min, final int max) {
        this(min, max, new MersenneTwister());
    }

    /**
     * @return the generator
     */
    protected final RandomGenerator getGenerator() {
        return g;
    }

    /**
     * @return the min
     */
    protected final int getMin() {
        return min;
    }

    /**
     * @return the max
     */
    protected final int getMax() {
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
    public void afterTick(final int t) {
        updateCurrentValue();
    }

    private void updateCurrentValue() {
        this.currentValue = calculateNewValue();
        this.totalProduction += this.currentValue;
    }

    /**
     * Calculates a new value for this output generator to provide in next time
     * step.
     * 
     * @return the new value for the next time step.
     */
    protected int calculateNewValue() {
        return getGenerator().nextInt(Math.abs(getMax() - getMin())) + getMin();
    }

    @Override
    public void tick(final int t) {
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    @Override
    public void initialize(final SimulationContext context) {
        this.g = context.getRandom();
    }
}
