package be.kuleuven.cs.flexsim.domain.tso;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Represents a TSO implementation with random signal.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public class RandomTSO implements SteeringSignal {
    private final RandomGenerator g = new MersenneTwister();
    private final int min;
    private final int max;

    /**
     * Default constructor.
     * 
     * @param min
     *            the minimum value for the random generator.
     * @param max
     *            the maximum value for the random generator.
     */
    public RandomTSO(int min, int max) {
        this.min = min;
        this.max = max;
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
    public int getCurrentValue(int timeMark) {
        return getGenerator().nextInt(Math.abs(getMax() - getMin())) + getMin();
    }

}
