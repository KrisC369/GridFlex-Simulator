package be.kuleuven.cs.flexsim.domain.energy.generation;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a TSO implementation with random signal from normally represented
 * distribution.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class WeighedNormalRandomOutputGenerator extends NormalRandomOutputGenerator {
    private final double weightOfNewVal;

    /**
     * Default constructor with a weight of 100% of the new value. Exactly
     * similar to the NormalRandomOutput version.
     *
     * @param min the minimum value for the random generator.
     * @param max the maximum value for the random generator.
     * @param g   The random generator to use.
     */
    WeighedNormalRandomOutputGenerator(final int min, final int max, final RandomGenerator g) {
        this(min, max, g, 1);
    }

    /**
     * Default constructor with a weight of 100% of the new value. Exactly
     * similar to the NormalRandomOutput version.
     *
     * @param min the minimum value for the random generator.
     * @param max the maximum value for the random generator.
     */
    public WeighedNormalRandomOutputGenerator(final int min, final int max) {
        this(min, max, 1);
    }

    /**
     * Default constructor.
     *
     * @param min    the minimum value for the random generator.
     * @param max    the maximum value for the random generator.
     * @param g      The random generator to use.
     * @param weight The weight to give to the new value. Must be between 0.0 and
     *               1.0
     */
    WeighedNormalRandomOutputGenerator(final int min, final int max, final RandomGenerator g,
            final double weight) {
        super(min, max, g);
        checkArgument(weight <= 1 && weight >= 0,
                "Weight must be between 0 and 1");
        this.weightOfNewVal = weight;
    }

    /**
     * Default constructor.
     *
     * @param min    the minimum value for the random generator.
     * @param max    the maximum value for the random generator.
     * @param weight The weight to give to the new value. Must be between 0.0 and
     *               1.0
     */
    public WeighedNormalRandomOutputGenerator(final int min, final int max, final double weight) {
        this(min, max, new MersenneTwister(), weight);
    }

    @Override
    protected int calculateNewValue() {
        final double oldVal = getLastStepProduction();
        final int newVal = super.calculateNewValue();
        return (int) Math.round(
                (newVal * weightOfNewVal) + (oldVal * (1 - weightOfNewVal)));
    }
}
