package be.kuleuven.cs.gridflex.domain.util.data;

import java.util.function.Function;

/**
 * /**
 * Represents a function that accepts a primitive double-valued argument and produces a
 * primitive double result.  This is the {@code double}-consuming primitive specialization for
 * {@link Function}.
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(double)}.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * @see Function
 */
@FunctionalInterface
public interface DoubleToDoubleFunction {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    double apply(double value);
}
