package be.kuleuven.cs.gridflex.solvers.memoization.immutableViews;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.io.Serializable;

/**
 * Immutable view of the problem context to be used as key-entry for the memoization framework.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class ImmutableSolverProblemContextView implements Serializable {

    private static final long serialVersionUID = -3237434526693894578L;

    ImmutableSolverProblemContextView() {
    }

    /**
     * @return The flexibility providers present for this problem context.
     */
    public abstract ImmutableList<FlexibilityProviderView> getProviders();

    /**
     * @return The energy profile to minimize as a goal for the solvers.
     */
    public abstract DoubleList getInputProfile();

    /**
     * @return the seed value. (default = 0)
     */
    public abstract long getSeedValue();

    /**
     * Static factory method
     *
     * @param context The modifiable context.
     * @return An unmodifiable view.
     */
    public static ImmutableSolverProblemContextView from(FlexAllocProblemContext context) {
        ImmutableList.Builder<FlexibilityProviderView> builder = ImmutableList.builder();
        for (FlexibilityProvider f : context.getProviders()) {
            builder.add(FlexibilityProviderView.from(f));
        }
        return new AutoValue_ImmutableSolverProblemContextView(builder.build(),
                context.getEnergyProfileToMinimizeWithFlex().values(), context.getSeedValue());
    }
}
