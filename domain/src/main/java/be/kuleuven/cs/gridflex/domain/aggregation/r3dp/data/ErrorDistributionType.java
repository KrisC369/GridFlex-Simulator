package be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data;

import be.kuleuven.cs.gridflex.domain.util.data.AbstractErrorDistribution;

/**
 * Specification of different statistical random distributions used for error generation.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public enum ErrorDistributionType {
    NORMAL(((seed, distribution) -> new MultiHorizonNormalErrorGenerator(seed, distribution))),
    CAUCHY(((seed, distribution) -> new MultiHorizonCauchyErrorGenerator(seed, distribution)));
    private final MultiHorizonErrorGeneratorFactory factory;

    ErrorDistributionType(MultiHorizonErrorGeneratorFactory factory) {

        this.factory = factory;
    }

    public static ErrorDistributionType from(String type) {
        if ("NORMAL".equalsIgnoreCase(type)) {
            return NORMAL;
        } else if ("CAUCHY".equalsIgnoreCase(type)) {
            return CAUCHY;
        } else {
            throw new IllegalArgumentException("Argument should be either NORMAL or CAUCHY.");
        }
    }

    /**
     * Create a generator object.
     *
     * @return a fully built multi horizon error generator.
     */
    public MultiHorizonErrorGenerator createErrorGenerator(long seed,
            AbstractErrorDistribution distribution) {
        return this.factory.createGenerator(seed, distribution);
    }

    /**
     * Functional interface for creating error generator instances.
     *
     * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
     */
    @FunctionalInterface
    interface MultiHorizonErrorGeneratorFactory {

        /**
         * Factory method to be implemented to create concrete generators.
         *
         * @param seed         The random seed to use.
         * @param distribution The error distribution to use.
         * @return A fully built and ready to use error generator.
         */
        MultiHorizonErrorGenerator createGenerator(long seed,
                AbstractErrorDistribution distribution);
    }
}
