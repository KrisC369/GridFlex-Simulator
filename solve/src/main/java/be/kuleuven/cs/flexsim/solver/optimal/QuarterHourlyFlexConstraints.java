package be.kuleuven.cs.flexsim.solver.optimal;

import be.kuleuven.cs.flexsim.domain.energy.dso.offline.r3dp.FlexConstraints;

/**
 * Concrete flexibility constraint representation.
 * All values are in a quarter hour time horizon unless specified otherwise.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface QuarterHourlyFlexConstraints extends FlexConstraints {
}
