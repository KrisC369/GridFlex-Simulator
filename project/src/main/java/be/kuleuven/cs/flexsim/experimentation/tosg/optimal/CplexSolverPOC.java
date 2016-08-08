package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import net.sf.jmpi.main.MpDirection;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.MpSolver;
import net.sf.jmpi.main.expression.MpExpr;
import net.sf.jmpi.solver.cplex.SolverCPLEX;

import static net.sf.jmpi.main.expression.MpExpr.prod;
import static net.sf.jmpi.main.expression.MpExpr.sum;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class CplexSolverPOC {

    public static void main(String[] args) {
        MpSolver solver = new SolverCPLEX();
        MpProblem prob = new MpProblem();
        prob.addVar("x", Integer.class);
        prob.addVar("y", Integer.class);
        MpExpr obj = sum(prod(143, "x"), prod(60, "y"));
        prob.setObjective(obj, MpDirection.MAX);

        prob.add(sum(prod(100, "x"), prod(20, "x"), prod(210, "y")), "<=", 15000);
        prob.add(sum(prod(110, "x"), prod(30, "y")), "<=", 4000);
        prob.add(sum("x"), "<=", sum(75, prod(-1, "y")));

        solver.add(prob);

        MpResult result = solver.solve();
        System.out.println(result);
    }
}
