/**
 *
 */
package be.kuleuven.cs.flexsim.domain.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;

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
     * This calculates and returns the element of the list for which the
     * application of f to that element reaches its maximum.
     *
     * @param list
     *            the list of elements.
     * @param f
     *            the function to apply to an element to get the value to
     *            compare with the maximum.
     * @param <T>
     *            the type representing the elements to apply function f to.
     * @return the argument attaining the maximum in f.
     */
    public static <T> T argMax(Iterable<T> list, IntNNFunction<T> f) {
        checkArgument(list.iterator().hasNext(), "Can't provide empty list to this function");
        int max = 0;
        Iterator<T> it = list.iterator();
        T currentMax = it.next();
        while (it.hasNext()) {
            T current = it.next();
            if (f.apply(current) > max) {
                max = f.apply(current);
                currentMax = current;
            }
        }
        return currentMax;
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
