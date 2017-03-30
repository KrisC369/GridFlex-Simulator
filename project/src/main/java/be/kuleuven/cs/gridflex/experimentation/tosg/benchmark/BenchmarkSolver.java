package be.kuleuven.cs.gridflex.experimentation.tosg.benchmark;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Heuristic solvers making use of optaplanner to find a solution to flex allocation problems.
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
                        "be/kuleuven/cs/gridflex/experimentation/configs"
                                + "/HeuristicPlannerBenchmarkConfig.xml");
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmark();

    }
}