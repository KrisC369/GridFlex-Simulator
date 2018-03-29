package be.kuleuven.cs.gridflex.domain.energy.dso.r3dp;

import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;

import java.util.Collection;

/**
 * Represents the context for which to solve flexibility allocation problems.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface FlexAllocProblemContext {
    /**
     * a default seed value. (default = 0)
     */
    long DEFAULT_SEED = 0L;

    /**
     * @return The flexibility providers present for this problem context.
     */
    Collection<FlexibilityProvider> getProviders();

    /**
     * @return The energy profile to minimize as a goal for the solvers.
     */
    TimeSeries getEnergyProfileToMinimizeWithFlex();

    /**
     * @return a default seed value. (default = 0)
     */
    default long getSeedValue() {
        return DEFAULT_SEED;
    }
}
