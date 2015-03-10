/**
 *
 */
package be.kuleuven.cs.flexsim.domain.util;


/**
 * This class represents some generic utility function making use of mapping
 * functions.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class CollectionUtils {
    private CollectionUtils() {
    }

    /**
     * This calculates the max of a certain value in a list of objects having
     * comparable values.
     *
     * @param list
     *            the list of elements.
     * @param f
     *            the function to apply to an element to get the value to
     *            compare with the maximum.
     * @param <T>
     *            the type representing the elements to apply function f to.
     * @return the maximum.
     */
    public static <T> int max(Iterable<T> list, IntNNFunction<T> f) {
        int max = 0;
        for (T t : list) {
            if (f.apply(t) > max) {
                max = f.apply(t);
            }
        }
        return max;
    }

    /**
     * This calculates the sum of a certain value over a list of objects having
     * that value.
     *
     * @param elems
     *            the list of elements.
     * @param f
     *            the function to apply to an element to get the value to sum.
     * @param <T>
     *            the type representing the elements to apply function f to.
     *            over.
     * @return the sum over all elements.
     */
    public static <T> int sum(Iterable<T> elems, IntNNFunction<T> f) {
        int tot = 0;
        for (T t : elems) {
            tot += f.apply(t);
        }
        return tot;
    }
}
