package be.kuleuven.cs.flexsim.experimentation.tosg.benchmark;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.solver.heuristic.domain.Allocation;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be.kuleuven.cs.flexsim.solver.heuristic.solver.HeuristicSolver.toDisplayString;

/**
 * Heuristic solver making use of optaplanner to find a solution to flex allocation problems.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class BenchmarkSolver {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkSolver.class);

    BenchmarkSolver() {
    }

    public static void main(String[] args) {
        new BenchmarkSolver().bench();
    }

    public void bench() {

        //

        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory
                .createFromXmlResource(
                        "HeuristicPlannerBenchmarkConfig.xml");
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmark();

    }
}