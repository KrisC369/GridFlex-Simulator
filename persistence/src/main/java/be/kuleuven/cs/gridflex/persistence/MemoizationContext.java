package be.kuleuven.cs.gridflex.persistence;

import com.google.common.base.Supplier;

/**
 * Memoization context for storing costly calculated results for later use without the calculation.
 * Serves as a sort of cache.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface MemoizationContext<E, R> {

    /**
     * Classic memoization function.
     * Before call, a lookup is made into the memoziation context cache with the specified entry.
     * If present, result for entry is returned.
     * If not, calculation function is called and the result of the call is interned before being
     * returned.
     *
     * @param entry         The entry into the table and possible context for calculation.
     * @param calculationFu The function for costly calculating results.
     * @param updateCache   True if you want cache updates. False is read-only cache.
     * @return The result of the calculation.
     */
    R testAndCall(E entry, Supplier<R> calculationFu, boolean updateCache);

    /**
     * Classic memoization function.
     * Before call, a lookup is made into the memoziation context cache with the specified entry.
     * If present, result for entry is returned.
     * If not, calculation function is called and the result of the call is interned before being
     * returned.
     *
     * @param entry         The entry into the table and possible context for calculation.
     * @param calculationFu The function for costly calculating results.
     * @return The result of the calculation.
     */
    default R testAndCall(E entry, Supplier<R> calculationFu) {
        return testAndCall(entry, calculationFu, true);
    }
}
