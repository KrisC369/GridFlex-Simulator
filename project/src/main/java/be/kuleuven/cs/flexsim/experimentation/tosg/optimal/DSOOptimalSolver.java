package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.MpSolver;
import net.sf.jmpi.main.MpVariable;
import net.sf.jmpi.main.expression.MpExpr;
import net.sf.jmpi.solver.cplex.SolverCPLEX;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DSOOptimalSolver extends OptimalSolver {
    private CongestionProfile profile;
    private MpSolver solverInstance;

    public DSOOptimalSolver(CongestionProfile c, int i) {
        profile = c;
        solverInstance = new SolverCPLEX();
    }

    @Override
    protected void processResults(MpResult result) {

    }

    @Override
    public MpSolver getSolver() {
        return this.solverInstance;
    }

    @Override
    public MpProblem getProblem() {
        MpProblem prob = new MpProblem();
        for (FlexProvider f : getProviders()) {
            prob.add(getDVarsFor(f));
        }

        prob.add(getGoal(prob));
        for (FlexProvider f : getProviders()) {
            prob.add(getConstraintsFor(f));
        }

        return prob;
    }

    private MpProblem getGoal(MpProblem tempProb) {
        //        obj = sum(prod(143, "x"), prod(60, "y"));
        //        prob.setObjective(obj, MpDirection.MAX);
        MpExpr goalExpr = new MpExpr();
        for(MpVariable v : tempProb.getVariables()){
            v.getVar();
        }

        return null;
    }

    private MpProblem getDVarsFor(FlexProvider pv) {
        MpProblem p = new MpProblem();
        MpAdapter adapt = new MpAdapter(pv.getActivationConstraints(), profile.length());
        for (MpVariable v : adapt.getDVars()) {
            p.add(v);
        }
        return p;
    }

    private MpProblem getConstraintsFor(FlexProvider pv) {
        MpProblem p = new MpProblem();
        MpAdapter adapt = new MpAdapter(pv.getActivationConstraints(), profile.length());
        for (MpConstraint v : adapt.getConstraints()) {
            p.add(v);
        }
        return p;
    }
}
