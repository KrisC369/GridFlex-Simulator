package be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;

/**
 * Generic factory interface for creating solvers.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface AbstractSolverFactory<T> {
    /**
     * Create solver.
     *
     * @param context The problem context.
     * @return A configured solver instance.
     */
    Solver<T> createSolver(FlexAllocProblemContext context);
}
