package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import autovalue.shaded.com.google.common.common.collect.Lists;
import autovalue.shaded.com.google.common.common.collect.Maps;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpDirection;
import net.sf.jmpi.main.MpOperator;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.MpSolver;
import net.sf.jmpi.main.MpVariable;
import net.sf.jmpi.main.expression.MpExpr;
import net.sf.jmpi.solver.cplex.SolverCPLEX;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static net.sf.jmpi.main.expression.MpExpr.prod;
import static net.sf.jmpi.main.expression.MpExpr.sum;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DSOOptimalSolver extends OptimalSolver {
    private CongestionProfile profile;
    private MpSolver solverInstance;
    private final List<String> congID;
    private final Map<FlexProvider, String> flexID;
    private ListMultimap<FlexProvider, String> allocDvarID;
    private AllocResults results;

    public DSOOptimalSolver(CongestionProfile c, int hor) {
        profile = c;
        solverInstance = new SolverCPLEX();
        congID = Lists.newArrayList();
        flexID = Maps.newLinkedHashMap();
        allocDvarID = ArrayListMultimap.create();
        for (int i = 0; i < profile.length(); i++) {
            congID.add("Cong:" + i);
        }
    }

    @Override
    protected void processResults(MpResult result) {
        System.out.println(result);
        List<Boolean> t = Lists.newArrayList();
        ListMultimap<FlexProvider, Boolean> allocResults = ArrayListMultimap.create();

        for (FlexProvider p : getProviders()) {
            for (String s : allocDvarID.get(p)) {
                t.add(result.getBoolean(s));
                allocResults.put(p, result.getBoolean(s));
            }
        }
        this.results = AllocResults.create(allocResults, result.getObjective().doubleValue());
    }

    @Override
    public MpSolver getSolver() {
        return this.solverInstance;
    }

    @Override
    public MpProblem getProblem() {
        MpProblem prob = new MpProblem();
        addDataToProblem(prob);

        int i = 0;
        for (FlexProvider f : getProviders()) {
            prob.add(getDVarsFor(f, i++));
        }

        addGoalToProb(prob);
        for (FlexProvider f : getProviders()) {
            addConstraintsForFlexToProb(prob, f);
        }

        return prob;
    }

    private void addDataToProblem(MpProblem prob) {
        //cong
        for (String s : congID) {
            prob.addVar(s, Double.class);
        }
        //congConstraints
        for (int i = 0; i < profile.length(); i++) {
            MpExpr lhs = new MpExpr().add(congID.get(i));
            MpExpr rhs = new MpExpr().add(profile.value(i));
            prob.add(new MpConstraint(lhs, MpOperator.EQ, rhs));

        }

        for (FlexProvider f : getProviders()) {
            String flexVar = getFlexID(f);
            flexID.put(f, flexVar);
            prob.addVar(flexVar, Integer.class);
            MpExpr lhs = new MpExpr().add(flexVar);
            MpExpr rhs = new MpExpr().add(f.getPowerRate());
            prob.add(new MpConstraint(lhs, MpOperator.EQ, rhs));
        }
    }

    private void addGoalToProb(MpProblem tempProb) {
        //        obj = sum(prod(143, "x"), prod(60, "y"));
        //        prob.setObjective(obj, MpDirection.MAX);
        MpExpr goalExpr = new MpExpr();

        for (int i = 0; i < profile.length(); i++) {
            for (FlexProvider j : getProviders()) {
                String flexJ = flexID.get(j);
                MpVariable flex = tempProb.getVariable(flexJ);
                MpVariable cong = tempProb.getVariable(congID.get(i));
                MpVariable alloc = tempProb.getVariable(allocDvarID.get(j).get(i));
                //                goalExpr.add(sum(cong, prod(-1, prod(alloc, flex))));
                goalExpr.add(sum(congID.get(i), prod(-1, prod(allocDvarID.get(j).get(i), flexJ))));

            }
        }
        tempProb.setObjective(goalExpr, MpDirection.MIN);
    }

    private MpProblem getDVarsFor(FlexProvider pv, int i) {
        MpProblem p = new MpProblem();
        //cong
        for (int c = 0; c < profile.length(); c++) {
            String alloc = "alloc:" + pv.hashCode() + ":" + i;
            allocDvarID.put(pv, alloc);
            p.addVar(alloc, Boolean.class);
        }

        //        MpAdapter adapt = new MpAdapter(pv.getActivationConstraints(), profile.length());
        //        for (MpVariable v : adapt.getDVars()) {
        //            p.add(v);
        //        }
        return p;
    }

    @NotNull
    private String getFlexID(FlexProvider pv) {
        return "Flex:" + pv.hashCode();
    }

    private void addConstraintsForFlexToProb(MpProblem p, FlexProvider pv) {
        //        MpProblem p = new MpProblem();

        //flexConstraints

        MpAdapter adapt = new MpAdapter(pv.getActivationConstraints(), profile.length(),
                allocDvarID.get(pv));
        for (MpConstraint v : adapt.getConstraints()) {
            p.add(v);
        }

    }

    public AllocResults getResults() {
        return this.results;
    }

}
