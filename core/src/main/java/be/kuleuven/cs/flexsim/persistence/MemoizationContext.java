package be.kuleuven.cs.flexsim.persistence;

import com.google.common.base.Supplier;

/**
 * Memoization context for storing costly calculated results for later use without the calculation.
 * Serves as a sort of cache.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface MemoizationContext<E, R> {

    /**
     * Classic memoiztion function.
     *
     * @param entry
     * @param calculationFu
     * @return
     */
    R testAndCall(E entry, Supplier<R> calculationFu);
}
