package be.kuleuven.cs.flexsim.solvers.memoization;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.persistence.MemoizationContext;
import be.kuleuven.cs.flexsim.solvers.AllocResults;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import com.google.common.base.Supplier;

/**
 * Decorator making use of memozation to avoid costly recalculation.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MemoizationDecorator implements Solver<AllocResults> {
    private final Solver<AllocResults> actualSolver;
    private FlexAllocProblemContext context;
    private final MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>
            memoization;
    private final ImmutableSolverProblemContextView contextView;

    /**
     * Constructor
     *
     * @param actualSolver
     * @param context
     * @param memoizationSupplier
     */
    public MemoizationDecorator(Solver<AllocResults> actualSolver, FlexAllocProblemContext context,
            Supplier<MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>>
                    memoizationSupplier) {
        this.actualSolver = actualSolver;
        this.context = context;
        this.memoization = memoizationSupplier.get();
        this.contextView = ImmutableSolverProblemContextView.from(context);
    }

    @Override
    public AllocResults solve() {

        return memoization.testAndCall(contextView, this::calculateResult, false)
                .toBackedView(context);
    }

    private AllocResultsView calculateResult() {
        return AllocResultsView.from(actualSolver.solve());
    }
}
