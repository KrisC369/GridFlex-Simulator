package be.kuleuven.cs.flexsim.solver;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.solver.heuristic.solver.HeuristicSolver;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import be.kuleuven.cs.flexsim.solver.optimal.mip.MIPOptimalSolver;

/**
 * Factory utility class for creating solver instances.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class Solvers {
    private Solvers() {
    }

    /**
     * Create a solver that solves for optimality using a MIP model and the Gurobi solver.
     *
     * @param context The problem context to solve.
     * @return An instantiated solver instance.
     */
    public static Solver<AllocResults> createMIPgurobi(FlexAllocProblemContext context) {
        return new MIPOptimalSolver(context, AbstractOptimalSolver.Solver.GUROBI);
    }

    /**
     * Create a solver that solves for optimality using a MIP model and the Cplex solver.
     *
     * @param context The problem context to solve.
     * @return An instantiated solver instance.
     */
    public static Solver<AllocResults> createMIPcplex(FlexAllocProblemContext context) {
        return new MIPOptimalSolver(context, AbstractOptimalSolver.Solver.CPLEX);
    }

    /**
     * Create a solver that searches the solution space heuristically using optaplanner.
     *
     * @param context The problem context to solve.
     * @param fullsat set to false if you want best effort allocation while possible leaving some
     *                constraints unbound.
     * @return An instantiated solver instance.
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
     * Create a dummy solver that returns hardcoded results for any input problem.
     *
     * @param context The problem context to solve.
     * @return An instantiated solver instance.
     */
    public static Solver<AllocResults> createDummySolver(FlexAllocProblemContext context) {
        return new MIPOptimalSolver(context, AbstractOptimalSolver.Solver.DUMMY);
    }

    public enum TYPE {
        DUMMY,
        OPTA,
        GUROBI,
        CPLEX;

        public Solver<AllocResults> getInstance(FlexAllocProblemContext context) {
            if (GUROBI == this) {
                return createMIPgurobi(context);

            } else if (CPLEX == this) {
                return createMIPcplex(context);
            } else if (OPTA == this) {
                return createHeuristicOptaplanner(context, true);
            } else {
                return createDummySolver(context);
            }
        }
    }
}
