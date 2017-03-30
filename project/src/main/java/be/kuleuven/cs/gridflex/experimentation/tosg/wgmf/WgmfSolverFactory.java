package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import be.kuleuven.cs.gridflex.experimentation.tosg.adapters.SolutionResultAdapter;
import be.kuleuven.cs.gridflex.experimentation.tosg.adapters.SolverAdapter;
import be.kuleuven.cs.gridflex.persistence.MemoizationContext;
import be.kuleuven.cs.gridflex.solvers.Solvers;
import be.kuleuven.cs.gridflex.solvers.data.AllocResults;
import be.kuleuven.cs.gridflex.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.gridflex.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * A solvers factory for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class WgmfSolverFactory
        implements AbstractSolverFactory<SolutionResults>, Serializable {
    private static final long serialVersionUID = -5851172788369007725L;
    private final Solvers.TYPE type;
    private final boolean updateCache;
    private Supplier<MemoizationContext<ImmutableSolverProblemContextView,
            AllocResultsView>> memoizationContext;
    private long defaultSeed = 0L;

    /**
     * Constructor with public visibility for testing purposes.
     *
     * @param type               the solver type.
     * @param updateCache        update the cache: Yes|no
     * @param memoizationContext The memoization context supplier.
     */
    public WgmfSolverFactory(Solvers.TYPE type, boolean updateCache,
            Supplier<MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>>
                    memoizationContext) {
        this.type = type;
        this.updateCache = updateCache;
        this.memoizationContext = memoizationContext;
    }

    public void setSeed(long seed) {
        this.defaultSeed = seed;
    }

    @Override
    public Solver<SolutionResults> createSolver(final FlexAllocProblemContext context) {
        final FlexAllocProblemContext flexAllocProblemContext = new FlexAllocProblemContext() {
            @Override
            public Collection<FlexibilityProvider> getProviders() {
                return context.getProviders();
            }

            @Override
            public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                return context.getEnergyProfileToMinimizeWithFlex();
            }

            @Override
            public long getSeedValue() {
                return defaultSeed;
            }
        };
        final Solver<AllocResults> solverInstance;
        if (memoizationContext.get() != null) {
            solverInstance = type
                    .getCachingInstance(flexAllocProblemContext, memoizationContext.get(),
                            updateCache);
        } else {
            solverInstance = type.getInstance(flexAllocProblemContext);
        }
        return new SolverAdapter<AllocResults, SolutionResults>(solverInstance) {

            @Override
            public SolutionResults adaptResult(AllocResults solution) {
                return new SolutionResultAdapter(solution).getResults();
            }
        };
    }
}
