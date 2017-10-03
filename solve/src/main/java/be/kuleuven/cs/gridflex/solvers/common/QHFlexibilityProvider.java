package be.kuleuven.cs.gridflex.solvers.common;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.solvers.optimal.ConstraintConversion;
import be.kuleuven.cs.gridflex.solvers.common.data.QuarterHourlyFlexConstraints;

/**
 * A quarter hourly discretized flexibility provider.
 *
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

    /**
     * @return The wrapped provider.
     */
    FlexibilityProvider getWrappedProvider();

}
