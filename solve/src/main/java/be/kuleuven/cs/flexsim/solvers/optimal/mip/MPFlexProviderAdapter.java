package be.kuleuven.cs.flexsim.solvers.optimal.mip;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexConstraints;
import be.kuleuven.cs.flexsim.solvers.optimal.MpAdapter;
import be.kuleuven.cs.flexsim.solvers.data.QuarterHourlyFlexConstraints;
import com.google.common.collect.Lists;
import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpOperator;
import net.sf.jmpi.main.MpVariable;
import net.sf.jmpi.main.expression.MpExpr;

import java.util.Collections;
import java.util.List;

import static net.sf.jmpi.main.expression.MpExpr.prod;
import static net.sf.jmpi.main.expression.MpExpr.sum;

/**
 * An adapter for flex providers working with DSOs.
 * This instance can generate DSO specific solvers constraints for a given FlexConstraint instance.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MPFlexProviderAdapter implements MpAdapter {
    private final FlexConstraints target;
    private final int profileSize;
    private final List<String> dvars;

    /**
     * Default constructor.
     *
     * @param activationConstraints The activation constraints from the Flex provider.
     * @param allocationIDs         the named list of dvar ID's to use to build the constraints.
     */
    public MPFlexProviderAdapter(final QuarterHourlyFlexConstraints activationConstraints,
            final List<String> allocationIDs) {
        this.target = activationConstraints;
        this.dvars = Lists.newArrayList(allocationIDs);
        this.profileSize = dvars.size();
    }

    /**
     * No decisions needed from providers.
     *
     * @return An empty list.
     */
    @Override
    public List<MpVariable> getDVars() {
        return Collections.emptyList();
    }

    @Override
    public List<MpConstraint> getConstraints() {
        final List<MpConstraint> constraints = Lists.newArrayList();

        //MaxBooleanVars
        getMaxBooleanActivationsConstraint(constraints);

        //interAct
        getInterActivationConstraints(constraints);

        //consecutiveActivation1
        getFirstConsecutiveActivationsConstraints(constraints);

        //consecutiveActivation2
        getSecondConsecutiveActivationsConstraints(constraints);

        //consecutiveActivation3
        getThirdConsecutiveActivationsConstraints(constraints);

        return constraints;
    }

    private void getMaxBooleanActivationsConstraint(final List<MpConstraint> constraints) {
        final MpExpr lhs = new MpExpr();
        for (int i = 0; i < profileSize; i++) {
            lhs.add(dvars.get(i));
        }
        final MpExpr rhs = new MpExpr()
                .add(target.getActivationDuration() * target.getMaximumActivations());
        constraints.add(new MpConstraint(lhs, MpOperator.EQ, rhs));
    }

    private void getInterActivationConstraints(final List<MpConstraint> constraints) {
        MpExpr lhs;
        MpExpr rhs;
        for (int i = 0; i
                < profileSize - target.getActivationDuration() - target.getInterActivationTime()
                + 1; i++) {
            lhs = new MpExpr();
            for (int k = 0;
                 k < target.getActivationDuration() + target.getInterActivationTime(); k++) {
                lhs.add(dvars.get(i + k));
            }
            rhs = new MpExpr().add(target.getActivationDuration());
            constraints.add(new MpConstraint(lhs, MpOperator.LE, rhs));
        }
    }

    private void getThirdConsecutiveActivationsConstraints(final List<MpConstraint> constraints) {
        MpExpr lhs;
        MpExpr rhs;
        for (int i = 0; i < profileSize - target.getActivationDuration(); i++) {
            lhs = new MpExpr();
            lhs.add(target.getActivationDuration());

            for (int k = 0; k < target.getActivationDuration(); k++) {
                lhs.add(prod(-1, dvars.get(i + k)));
            }
            lhs.add(sum(1, prod(-1, dvars.get((int) (i + target.getActivationDuration())))));
            rhs = new MpExpr().add(1);
            constraints.add(new MpConstraint(lhs, MpOperator.GE, rhs));
        }
    }

    private void getSecondConsecutiveActivationsConstraints(final List<MpConstraint> constraints) {
        MpExpr lhs;
        MpExpr rhs;
        for (int k = 0; k < target.getActivationDuration(); k++) {
            lhs = new MpExpr();
            lhs.add(sum(dvars.get(k), prod(-1, dvars.get(k + 1))));
            rhs = new MpExpr().add(0);
            constraints.add(new MpConstraint(lhs, MpOperator.LE, rhs));
        }
    }

    private void getFirstConsecutiveActivationsConstraints(final List<MpConstraint> constraints) {
        MpExpr lhs;
        MpExpr rhs;
        for (int i = (int) target.getActivationDuration(); i < profileSize - 1; i++) {
            lhs = new MpExpr();
            final double d1 = -1 * target.getActivationDuration() + 1;
            lhs.add(d1);
            for (int l = 0; l < target.getActivationDuration(); l++) {
                lhs.add(dvars.get(i - l));
            }
            lhs.add(prod(-10, dvars.get(i))).add(new MpExpr().add(9.999))
                    .add(prod(10.001, dvars.get(i + 1)));
            rhs = new MpExpr().add(0);
            constraints.add(new MpConstraint(lhs, MpOperator.GE, rhs));
        }
    }

}
