package be.kuleuven.cs.flexsim.domain.util;

/**
 * Determines an output int value based on an input value.
 * <p>
 * See the Guava User Guide article on
 * <a href= "http://code.google.com/p/guava-libraries/wiki/FunctionalExplained">
 * the use of {@code Function}</a> for more information on a similar function
 * interface that is Nullable.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <F>
 *            Input type.
 * 
 */
public interface IntNNFunction<F> {
    /**
     * Returns the result of applying this function to {@code input}. This
     * method is <i>generally expected</i>, but not absolutely required, to have
     * the following properties:
     * 
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>The computation is <i>consistent with equals</i>;
     * </ul>
     * 
     * @param input
     *            the input argument.
     * @return the result, as an integer, of applying this function to the
     *         input.
     * 
     * @throws NullPointerException
     *             if {@code input} is null and this function does not accept
     *             null arguments
     */
    int apply(F input);
}
