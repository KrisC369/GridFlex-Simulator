package be.kuleuven.cs.gridflex.domain.energy.generation;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Represents a TSO implementation with random signal from normally represented
 * distribution.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class NormalRandomOutputGenerator extends RandomOutputGenerator {
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
    NormalRandomOutputGenerator(final int min, final int max, final RandomGenerator g) {
        super(min, max, g);
    }

    /**
     * Default constructor.
     * 
     * @param min
     *            the minimum value for the random generator.
     * @param max
     *            the maximum value for the random generator.
     */
    public NormalRandomOutputGenerator(final int min, final int max) {
        super(min, max);
    }

    @Override
    protected int calculateNewValue() {
        final double first = getGenerator().nextDouble();
        final double second = getGenerator().nextDouble() * -1;
        double value = getGenerator().nextDouble();
        value = value * (first - second) - first;
        if (value < 0) {
            value *= Math.abs(this.getMin());
        } else {
            value *= this.getMax();
        }
        return (int) Math.round(value);
    }
}
