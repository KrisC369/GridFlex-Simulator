package be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;

/**
 * Generic factory interface for creating solvers.
 *
 * @param <T> The spefic solution type for the solvers instance returned by this factory.
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@FunctionalInterface
public interface AbstractSolverFactory<T> {
    /**
     * Create solvers.
     *
     * @param context The problem context.
     * @return A configured solvers instance.
     */
    Solver<T> createSolver(FlexAllocProblemContext context);
}
