package be.kuleuven.cs.flexsim.solvers.memoization;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.persistence.MemoizationContext;
import be.kuleuven.cs.flexsim.solvers.optimal.AllocResults;

/**
 * Decorator making use of memozation to avoid costly recalculation.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MemoizationDecorator implements Solver<AllocResults> {
    private final Solver<AllocResults> actualSolver;
    private final FlexAllocProblemContext context;
    private final MemoizationContext<FlexAllocProblemContext, AllocResults> memoization;

    public MemoizationDecorator(Solver<AllocResults> actualSolver, FlexAllocProblemContext context,
            MemoizationContext<FlexAllocProblemContext, AllocResults> memoization) {
        this.actualSolver = actualSolver;
        this.context = context;
        this.memoization = memoization;
    }

    @Override
    public AllocResults solve() {
        if (memoization.hasResultFor(context)) {
            AllocResults cachedResult = memoization.getMemoizedResultFor(context);
            if (cachedResult.equals(null)) {
                throw new IllegalStateException(
                        "Return from memoziation should not be null at this point.");
            }
            return cachedResult;
        } else {
            return intern(actualSolver.solve());
        }
    }

    private AllocResults intern(AllocResults solution) {
        this.memoization.memoizeEntry(context, solution);
        return solution;
    }
}
