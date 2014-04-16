package be.kuleuven.cs.flexsim.domain.util;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * * Determines an output value based on an input value.
 ** <p>
 * See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/FunctionalExplained">the use
 * of {@code Function}</a> for more information on a similar function interface
 * that is Nullable.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * @param <F>
 *            Input type.
 * @param <T>
 *            Output type.
 * 
 */
public interface NonNullableFunction<F, T> {

    /**
     * Returns the result of applying this function to {@code input}. This
     * method is <i>generally expected</i>, but not absolutely required, to have
     * the following properties:
     * 
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>The computation is <i>consistent with equals</i>; that is,
     * {@link Objects#equal Objects.equal}{@code (a, b)} implies that
     * {@code Objects.equal(function.apply(a), function.apply(b))}.
     * </ul>
     * 
     * @throws NullPointerException
     *             if {@code input} is null and this function does not accept
     *             null arguments
     */
    T apply(@Nullable F input);

    // @Override
    // boolean equals(@Nullable Object object);
}
