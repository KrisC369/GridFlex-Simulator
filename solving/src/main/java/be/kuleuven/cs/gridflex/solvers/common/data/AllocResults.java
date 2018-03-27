package be.kuleuven.cs.gridflex.solvers.common.data;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Spefic solving instance allocation results.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class AllocResults {
    /**
     * Represents an infeasible solution.
     */
    public static final AllocResults INFEASIBLE = create(ArrayListMultimap.create(), -1);

    /**
     * @return The allocation results as a map of provider to list of booleans indicating
     * activation moments.
     */
    public abstract ListMultimap<FlexibilityProvider, Boolean> getAllocationResults();

    /**
     * @return The objective function result value.
     */
    public abstract double getObjective();

    /**
     * If the maximum unconstrained objective value is known, or can be computed, returns the
     * percentage of this maximum achieved.
     * Can be negative if not known.
     *
     * @return the relative percentage or negative if not known or relevant.
     */
    public abstract double getObjectiveRelativeToUnconstrainedOptimal();

    /**
     * Factory method.
     *
     * @param allocs      The allocation results.
     * @param objective   The objective function value.
     * @param relativeObj the relative obj value.
     * @return A value object containing the results.
     */
    public static AllocResults create(final ListMultimap<FlexibilityProvider, Boolean> allocs,
            final double objective, final double relativeObj) {
        return new AutoValue_AllocResults(allocs, objective, relativeObj);
    }

    /**
     * Factory method.
     *
     * @param allocs    The allocation results.
     * @param objective The objective function value.
     * @return A value object containing the results.
     */
    public static AllocResults create(final ListMultimap<FlexibilityProvider, Boolean> allocs,
            final double objective) {
        return new AutoValue_AllocResults(allocs, objective, -1d);
    }

}
