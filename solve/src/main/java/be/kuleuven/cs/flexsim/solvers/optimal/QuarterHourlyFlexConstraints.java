package be.kuleuven.cs.flexsim.solvers.optimal;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexConstraints;

/**
 * Concrete flexibility constraint representation.
 * All values are in a quarter hour time horizon unless specified otherwise.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface QuarterHourlyFlexConstraints extends FlexConstraints {
}
