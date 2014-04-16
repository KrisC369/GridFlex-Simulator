/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.util;

import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class CollectionUtils {
    public static <T> int max(List<T> list, IntNNFunction<T> f) {
        int max = 0;
        for (T t : list) {
            if (f.apply(t) > max) {
                max = f.apply(t);
            }
        }
        return max;
    }

    public static <T> long sum(List<T> elems, LongNNFunction<T> f) {
        long tot = 0;
        for (T t : elems) {
            tot += f.apply(t);
        }
        return tot;
    }
}
