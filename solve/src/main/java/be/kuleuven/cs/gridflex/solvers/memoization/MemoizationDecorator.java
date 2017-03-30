package be.kuleuven.cs.gridflex.solvers.memoization;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.persistence.MemoizationContext;
import be.kuleuven.cs.gridflex.solvers.data.AllocResults;
import be.kuleuven.cs.gridflex.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.gridflex.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator making use of memozation to avoid costly recalculation.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MemoizationDecorator implements Solver<AllocResults> {
    private final Solver<AllocResults> actualSolver;
    private final FlexAllocProblemContext context;
    private final MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>
            memoization;
    private final ImmutableSolverProblemContextView contextView;
    private final boolean updateCache;
    private static final Logger logger = LoggerFactory.getLogger(MemoizationDecorator.class);

    /**
     * Constructor for decorating another solver solver.
     *
     * @param actualSolver        The actual solver to decorate.
     * @param context             The problem context.
     * @param memoizationSupplier The memoization service for storing caching results.
     */
    public MemoizationDecorator(Solver<AllocResults> actualSolver, FlexAllocProblemContext context,
            Supplier<MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>>
                    memoizationSupplier, boolean updateCache) {
        this.actualSolver = actualSolver;
        this.context = context;
        this.memoization = memoizationSupplier.get();
        this.contextView = ImmutableSolverProblemContextView.from(context);
        this.updateCache = updateCache;
    }

    @Override
    public AllocResults solve() {
        return memoization.testAndCall(contextView, this::calculateResult, updateCache)
                .toBackedView(context);
    }

    private AllocResultsView calculateResult() {
        return AllocResultsView.from(actualSolver.solve());
    }
}
