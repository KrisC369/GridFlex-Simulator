package be.kuleuven.cs.flexsim.solver.optimal;

import be.kuleuven.cs.flexsim.domain.energy.dso.offline.r3dp.FlexConstraints;

/**
 * A forwarder that multiplies the constraints in flex constrains with a given factor for e.g.
 * converting from hours to quarter hours.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class ConstraintStepMultiplierDecorator extends FlexConstraints {

    private final int factor;
    private final FlexConstraints target;

    /**
     * Default constructor
     *
     * @param constr The target to decorate.
     * @param factor The factor to multiply with.
     */
    public ConstraintStepMultiplierDecorator(final FlexConstraints constr, final int factor) {
        this.factor = factor;
        this.target = constr;
    }

    @Override
    public double getInterActivationTime() {
        return factor * target.getInterActivationTime();
    }

    @Override
    public double getActivationDuration() {
        return factor * target.getActivationDuration();
    }

    @Override
    public double getMaximumActivations() {
        return target.getMaximumActivations();
    }

    @Override
    public boolean equals(Object o) {
        return target.equals(o);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }
}
