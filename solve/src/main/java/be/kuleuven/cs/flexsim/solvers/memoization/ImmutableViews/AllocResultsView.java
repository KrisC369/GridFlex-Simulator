package be.kuleuven.cs.flexsim.solvers.memoization.immutableViews;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.solvers.optimal.AllocResults;
import com.google.auto.value.AutoValue;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * View of allocresults to be used in memoization context.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class AllocResultsView implements Serializable {

    private static final long serialVersionUID = 5336578454735826891L;

    /**
     * @return The allocation results as a map of provider to list of booleans indicating
     * activation moments.
     */
    public abstract ListMultimap<FlexibilityProviderView, Boolean> getAllocationResults();

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
     * Static factory method
     *
     * @param res The modifiable results.
     * @return An unmodifiable view.
     */
    public static AllocResultsView from(AllocResults res) {
        ListMultimap<FlexibilityProvider, Boolean> allocationResults = res.getAllocationResults();
        LinkedListMultimap<FlexibilityProviderView, Boolean> viewMap = LinkedListMultimap
                .create();
        for (FlexibilityProvider fp : allocationResults.keySet()) {
            viewMap.putAll(FlexibilityProviderView.from(fp), allocationResults.get(fp));
        }
        return new AutoValue_AllocResultsView(viewMap, res.getObjective(),
                res.getObjectiveRelativeToUnconstrainedOptimal());
    }

    public AllocResults toBackedView(FlexAllocProblemContext context) {
        ListMultimap<FlexibilityProviderView, Boolean> allocationResults = this
                .getAllocationResults();
        LinkedListMultimap<FlexibilityProvider, Boolean> origMap = LinkedListMultimap
                .create();
        List<FlexibilityProvider> providers = Lists.newArrayList(context.getProviders());
        List<FlexibilityProviderView> views = Lists.newArrayList(allocationResults.keySet());
        checkArgument(providers.size() == views.size(),
                "Both view and context should have the same number of providers.");
        for (int i = 0; i < providers.size(); i++) {
            if (!views.get(i).isViewOf(providers.get(i))) {
                throw new IllegalArgumentException(
                        "Ordering should be preserved within conversion from view to backed "
                                + "version.");
            }
            origMap.putAll(providers.get(i), allocationResults.get(views.get(i)));
        }
        return AllocResults
                .create(origMap, getObjective(), getObjectiveRelativeToUnconstrainedOptimal());
    }
}
