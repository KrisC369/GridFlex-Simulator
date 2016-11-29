package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Flexibility allocation results from optimization.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class SolutionResults {
    /**
     * Empty results.
     */
    public static final SolutionResults EMPTY = create(ArrayListMultimap.create(), 0, 4);
    public static final SolutionResults INFEASIBLE = create(ArrayListMultimap.create(), -1, 4);

    /**
     * @return A map of flexibility profile to their allocation schedule.
     */
    public abstract ListMultimap<FlexibilityProvider, Boolean> getAllocationMaps();

    /**
     * @return The objective value of this solution.
     */
    public abstract double getObjectiveValue();

    /**
     * @return The number of descretization time slots per hour.
     */
    public abstract int getDiscretisationInNbSlotsPerHour();

    /**
     * Default factory method.
     *
     * @param allocs       The allocation map.
     * @param slotsPerHour The number of slots per hour.
     * @return A SolutionResult instance.
     */
    public static SolutionResults create(ListMultimap<FlexibilityProvider, Boolean> allocs,
            double objectiveValue, int slotsPerHour) {
        return new AutoValue_SolutionResults(allocs, objectiveValue, slotsPerHour);
    }
}
