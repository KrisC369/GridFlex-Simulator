/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.util;

import java.util.List;

/**
 * This class represents some generic utility function making use of mapping
 * functions.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class CollectionUtils {
    /**
     * This calculates the max of a certain value in a list of objects having
     * comparable values.
     * 
     * @param list
     *            the list of elements
     * @param f
     *            the function to apply to an element to get the value to
     *            compare with the maximum.
     * @return the maximum.
     */
    public static <T> int max(List<T> list, IntNNFunction<T> f) {
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
     *            the function to apply to an element to get the value to sum
     *            over.
     * @return the sum over all elements.
     */
    public static <T> int sum(List<T> elems, IntNNFunction<T> f) {
        int tot = 0;
        for (T t : elems) {
            tot += f.apply(t);
        }
        return tot;
    }
}
