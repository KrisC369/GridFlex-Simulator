package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import net.sf.jmpi.main.MpDirection;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.MpSolver;
import net.sf.jmpi.main.expression.MpExpr;
import net.sf.jmpi.solver.cplex.SolverCPLEX;
import net.sf.jmpi.solver.gurobi.SolverGurobi;

import java.io.FileNotFoundException;
import java.io.IOException;

import static net.sf.jmpi.main.expression.MpExpr.prod;
import static net.sf.jmpi.main.expression.MpExpr.sum;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class CplexSolverPOC {

    private CongestionProfile profile;
    private DSOOptimalSolver solver;
    private FlexProvider provider1;
    private FlexProvider provider2;
    private FlexConstraints constraints;
    private static final String column = "test";
    private static final String file = "test.csv";

    CplexSolverPOC() {
        provider1 = new FlexProvider(200, FlexConstraints.R3DP);
        provider2 = new FlexProvider(500, FlexConstraints.R3DP);
    }

    void configModel(final DSOOptimalSolver.Solver s) {
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
            solver = new DSOOptimalSolver(profile, s);

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        constraints = FlexConstraints.builder().interActivationTime(6).interActivationTime(4)
                .maximumActivations(20).build();

        solver = new DSOOptimalSolver(profile, AbstractOptimalSolver.Solver.CPLEX);
        provider1 = new FlexProvider(200, constraints);
        provider2 = new FlexProvider(500, constraints);

    }

    void runModel() {
        solver.registerFlexProvider(provider1);
        solver.registerFlexProvider(provider2);
        solver.tick(1);
        final AllocResults res = solver.getResults();
    }

    public static void main(final String[] args) {
        //        poc();
        scratchPad();
    }

    public static void scratchPad() {
        final int times = 5;
        long total = 0;
        for (int i = 0; i < times; i++) {
            final CplexSolverPOC s = new CplexSolverPOC();
            s.configModel(AbstractOptimalSolver.Solver.CPLEX);
            final long before = System.currentTimeMillis();
            s.runModel();
            final long after = System.currentTimeMillis();
            total += (after - before);
        }
        final long cplex = (long) ((total / (double) times) / (double) 1000);
        //        System.out.println(
        //                "Cplex took me: " + (total / (double) times) / (double) 1000 + "
        // seconds.");

        total = 0;
        for (int i = 0; i < times; i++) {
            final CplexSolverPOC s = new CplexSolverPOC();
            s.configModel(AbstractOptimalSolver.Solver.GUROBI);
            final long before = System.currentTimeMillis();
            s.runModel();
            final long after = System.currentTimeMillis();
            total += (after - before);
        }

        final long gurobi = (long) ((total / (double) times) / (double) 1000);
        System.out.println("Cplex took me: " + cplex + " seconds.");
        System.out.println("Gurobi took me: " + gurobi + " seconds.");
    }

    public static void poc() {
        MpSolver solver = new SolverCPLEX();
        MpProblem prob = new MpProblem();
        prob.addVar("x", Integer.class);
        prob.addVar("y", Integer.class);
        MpExpr obj = sum(prod(143, "x"), prod(60, "y"));
        prob.setObjective(obj, MpDirection.MAX);

        prob.add(sum(prod(100, "x"), prod(20, "x"), prod(210, "y")), "<=", 1500000);
        prob.add(sum(prod(110, "x"), prod(30, "y")), "<=", 400000);
        prob.add(sum("x"), "<=", sum(75, prod(-1, "y")));

        solver.add(prob);

        MpResult result = solver.solve();
        System.out.println(result);

        solver = new SolverGurobi();
        prob = new MpProblem();
        prob.addVar("x", Integer.class);
        prob.addVar("y", Integer.class);
        obj = sum(prod(143, "x"), prod(60, "y"));
        prob.setObjective(obj, MpDirection.MAX);

        prob.add(sum(prod(100, "x"), prod(20, "x"), prod(210, "y")), "<=", 1500000);
        prob.add(sum(prod(110, "x"), prod(30, "y")), "<=", 400000);
        prob.add(sum("x"), "<=", sum(75, prod(-1, "y")));

        solver.add(prob);
        result = solver.solve();
        System.out.println(result);
        System.out.println(prob);

    }
}
