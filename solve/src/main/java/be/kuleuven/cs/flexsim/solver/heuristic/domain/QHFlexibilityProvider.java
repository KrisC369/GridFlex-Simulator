package be.kuleuven.cs.flexsim.solver.heuristic.domain;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.solver.optimal.ConstraintConversion;
import be.kuleuven.cs.flexsim.solver.optimal.QuarterHourlyFlexConstraints;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface QHFlexibilityProvider extends FlexibilityProvider {
    /**
     * Automatic conversion from hourly to quarter hourly converstion.
     *
     * @return a quarter hourly activation constraint object.
     */
    default QuarterHourlyFlexConstraints getQHFlexibilityActivationConstraints() {
        return ConstraintConversion.fromHourlyToQuarterHourly(
                getFlexibilityActivationConstraints());
    }

    FlexibilityProvider getWrappedProvider();
}
