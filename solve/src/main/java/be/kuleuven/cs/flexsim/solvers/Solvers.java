package be.kuleuven.cs.flexsim.solvers;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.persistence.MemoizationContext;
import be.kuleuven.cs.flexsim.solvers.dummy.SolverDummy;
import be.kuleuven.cs.flexsim.solvers.heuristic.solver.HeuristicSolver;
import be.kuleuven.cs.flexsim.solvers.memoization.MemoizationDecorator;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import be.kuleuven.cs.flexsim.solvers.optimal.mip.MIPOptimalSolver;

import static be.kuleuven.cs.flexsim.solvers.optimal.AbstractOptimalSolver.Solver.CPLEX;
import static be.kuleuven.cs.flexsim.solvers.optimal.AbstractOptimalSolver.Solver.GUROBI;

/**
 * Factory utility class for creating solvers instances.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class Solvers {

    private Solvers() {
    }

    /**
     * Create a solvers that solves for optimality using a MIP model and the Gurobi solvers.
     *
     * @param context The problem context to solve.
     * @return An instantiated solvers instance.
     */
    public static Solver<AllocResults> createMIPgurobi(FlexAllocProblemContext context) {
        return new MIPOptimalSolver(context, GUROBI);
    }

    /**
     * Create a solvers that solves for optimality using a MIP model and the Cplex solvers.
     *
     * @param context The problem context to solve.
     * @return An instantiated solvers instance.
     */
    public static Solver<AllocResults> createMIPcplex(FlexAllocProblemContext context) {
        return new MIPOptimalSolver(context, CPLEX);
    }

    /**
     * Create a solvers that searches the solution space heuristically using optaplanner.
     *
     * @param context The problem context to solve.
     * @param fullsat set to false if you want best effort allocation while possible leaving some
     *                constraints unbound.
     * @return An instantiated solvers instance.
     */
    public static Solver<AllocResults> createHeuristicOptaplanner(FlexAllocProblemContext context,
            boolean fullsat) {
        if (fullsat) {
            return HeuristicSolver.createFullSatHeuristicSolver(context);
        } else {
            return HeuristicSolver.createBestEffortHeuristicSolver(context);
        }
    }

    /**
     * Create a dummy solvers that returns hardcoded results for any input problem.
     *
     * @param context The problem context to solve.
     * @return An instantiated solvers instance.
     */
    public static Solver<AllocResults> createDummySolver(FlexAllocProblemContext context) {
        return new SolverDummy(context);
    }

    public enum TYPE {
        DUMMY,
        OPTA,
        OPTA_BEST_EFFORT,
        GUROBI,
        CPLEX;

        public Solver<AllocResults> getInstance(FlexAllocProblemContext context) {
            if (GUROBI == this) {
                return createMIPgurobi(context);
            } else if (CPLEX == this) {
                return createMIPcplex(context);
            } else if (OPTA == this) {
                return createHeuristicOptaplanner(context, true);
            } else if (OPTA_BEST_EFFORT == this) {
                return createHeuristicOptaplanner(context, false);
            } else {
                return createDummySolver(context);
            }
        }

        public Solver<AllocResults> getCachingInstance(FlexAllocProblemContext context,
                MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>
                        memContext, boolean update) {
            return new MemoizationDecorator(getInstance(context), context, () ->
                    memContext, update);
        }
    }
}
