package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class SolutionResults {
    public static final SolutionResults EMPTY = create(ArrayListMultimap.create());

    public abstract ListMultimap<FlexibilityProvider, Boolean> getAllocationMaps();

    public static SolutionResults create(ListMultimap<FlexibilityProvider, Boolean> allocs) {
        return new AutoValue_SolutionResults(allocs);
    }
}
