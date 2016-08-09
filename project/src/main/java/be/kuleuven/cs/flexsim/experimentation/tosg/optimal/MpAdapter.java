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

    public MpAdapter(FlexConstraints activationConstraints, int profileSize) {
        this.target = activationConstraints;
        this.profileSize = profileSize;
        this.constraints = Lists.newArrayList();
        this.dvars = Lists.newArrayList();
        for (int i = 0; i < target.getMaximumActivations(); i++) {
            dvars.add("alloc:" + target.toString() + ":" + String.valueOf(i));
        }
    }

    public List<MpVariable> getDVars() {
        List<MpVariable> vars = Lists.newArrayList();

        for (int i = 0; i < target.getMaximumActivations(); i++) {
            vars.add(new MpVariable(dvars.get(i), 0,
                    profileSize, MpVariable.Type.INT));
        }
        return vars;
    }

    public List<MpConstraint> getConstraints() {
        List<MpConstraint> constraints = Lists.newArrayList();

        for (int i = 0; i < target.getMaximumActivations() - 1; i++) {
            MpExpr e = new MpExpr().add(sum(dvars.get(i))).add(prod(-1, dvars.get(i + 1)))
                    .addTerm(target.getInterActivationTime())
                    .addTerm(target.getActivationDuration());
            constraints.add(new MpConstraint(e, MpOperator.LE, new MpExpr().add(0)));
        }
        //        prob.add(sum(prod(100, "x"), prod(20, "x"), prod(210, "y")), "<=", 1500000);
        return constraints;
    }
}
