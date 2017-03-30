package be.kuleuven.cs.gridflex.solvers.optimal;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexConstraints;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.solvers.data.QuarterHourlyFlexConstraints;

/**
 * Utility class for converting flex constraints from and to different time horizons.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class ConstraintConversion {

    private static final int HOUR_TO_QUARTER_HOUR = 4;

    private ConstraintConversion() {
    }

    /**
     * Convert from hourly flex constraints to quarter hourly constraints.
     *
     * @param c the hourly fle constraints to convert
     * @return a quarter hourly constraint instance
     */
    public static QuarterHourlyFlexConstraints fromHourlyToQuarterHourly(HourlyFlexConstraints c) {
        return new ConcreteQuarterHourlyConstraints(c, HOUR_TO_QUARTER_HOUR);
    }

    static class ConstraintStepMultiplierDecorator implements FlexConstraints {

        private final int factor;
        private final HourlyFlexConstraints target;

        /**
         * Default constructor
         *
         * @param constr The target to decorate.
         * @param factor The factor to multiply with.
         */
        ConstraintStepMultiplierDecorator(final HourlyFlexConstraints constr,
                final int factor) {
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
    }

    static class ConcreteQuarterHourlyConstraints extends ConstraintStepMultiplierDecorator
            implements QuarterHourlyFlexConstraints {

        /**
         * Default constructor
         *
         * @param constr The target to decorate.
         * @param factor The factor to multiply with.
         */
        ConcreteQuarterHourlyConstraints(
                HourlyFlexConstraints constr, int factor) {
            super(constr, factor);
        }
    }
}
