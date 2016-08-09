package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import autovalue.shaded.com.google.common.common.collect.Lists;
import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpOperator;
import net.sf.jmpi.main.MpVariable;
import net.sf.jmpi.main.expression.MpExpr;

import java.util.List;

import static net.sf.jmpi.main.expression.MpExpr.prod;
import static net.sf.jmpi.main.expression.MpExpr.sum;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MpAdapter {
    private FlexConstraints target;
    private int profileSize;
    private List<MpExpr> constraints;
    private List<String> dvars;

    public MpAdapter(FlexConstraints activationConstraints, int profileSize,
            List<String> allocationIDs) {
        this.target = activationConstraints;
        this.profileSize = profileSize;
        this.constraints = Lists.newArrayList();
        this.dvars = Lists.newArrayList(allocationIDs);
        //        for (int i = 0; i < profileSize; i++) {
        //            dvars.add("alloc:" + target.hashCode() + ":" + String.valueOf(i));
        //        }
        //        for (int i = 0; i < profileSize; i++) {
        //            vars.add(new MpVariable(dvars.get(i), 0, 1, MpVariable.Type.BOOL));
        //        }
    }

    public List<MpVariable> getDVars() {
        //        List<MpVariable> vars = Lists.newArrayList();
        //
        //        for (int i = 0; i < target.getMaximumActivations(); i++) {
        //            vars.add(new MpVariable(dvars.get(i), 0, 1, MpVariable.Type.BOOL));
        //        }
        return Lists.newArrayList();
    }

    public List<MpConstraint> getConstraints() {

        List<MpConstraint> constraints = Lists.newArrayList();

        //        for (int i = 0; i < target.getMaximumActivations() - 1; i++) {
        //            MpExpr e = new MpExpr().add(sum(dvars.get(i))).add(prod(-1, dvars.get(i + 1)))
        //                    .addTerm(target.getInterActivationTime())
        //                    .addTerm(target.getActivationDuration());
        //            constraints.add(new MpConstraint(e, MpOperator.LE, new MpExpr().add(0)));
        //        }
        //        prob.add(sum(prod(100, "x"), prod(20, "x"), prod(210, "y")), "<=", 1500000);

        //MaxBooleanVars
        MpExpr lhs = new MpExpr();
        for (int i = 0; i < profileSize; i++) {
            lhs.add(dvars.get(i));
        }
        MpExpr rhs = new MpExpr()
                .add(target.getActivationDuration() * target.getMaximumActivations());
        constraints.add(new MpConstraint(lhs, MpOperator.EQ, rhs));

        //interAct
        for (int i = 0; i
                < profileSize - target.getActivationDuration() - target.getInterActivationTime()
                + 1; i++) {
            lhs = new MpExpr();
            for (int k = 0;
                 k < target.getActivationDuration() + target.getInterActivationTime() - 1; k++) {
                lhs.add(dvars.get(i + k));
            }
            rhs = new MpExpr().add(target.getActivationDuration());
            constraints.add(new MpConstraint(lhs, MpOperator.LE, rhs));
        }
        //consecutiveActivation1
        for (int i = target.getActivationDuration(); i < profileSize; i++) {
            lhs = new MpExpr();
            double d1 = -1 * target.getActivationDuration() + 1;
            lhs.add(d1);
            for (int l = 0; l < target.getActivationDuration() - 1; l++) {
                lhs.add(dvars.get(i + l));
            }
            lhs.add(prod(-10, dvars.get(i))).add(prod(20, dvars.get(i)));
            rhs = new MpExpr().add(0);
            constraints.add(new MpConstraint(lhs, MpOperator.GE, rhs));
        }

        //consecutiveActivation2
        for (int k = 0; k < target.getActivationDuration() - 1; k++) {
            lhs = new MpExpr();
            lhs.add(sum(dvars.get(k), prod(-1, dvars.get(k + 1))));
            rhs = new MpExpr().add(0);
            constraints.add(new MpConstraint(lhs, MpOperator.LE, rhs));
        }

        //consecutiveActivation3
        for (int i = 0; i < profileSize - target.getActivationDuration() - 1; i++) {
            lhs = new MpExpr();
            lhs.add(target.getActivationDuration());

            for (int k = 0; k < target.getActivationDuration() - 1; k++) {
                lhs.add(dvars.get(i + k));
            }
            lhs.add(sum(1, prod(-1, dvars.get(i + target.getActivationDuration()))));
            rhs = new MpExpr().add(1);
            constraints.add(new MpConstraint(lhs, MpOperator.GE, rhs));
        }

        return constraints;
    }
}
