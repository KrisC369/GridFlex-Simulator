package be.kuleuven.cs.gridflex.solvers.common.data;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexConstraints;

/**
 * Concrete flexibility constraint representation.
 * All values are in a quarter hour time horizon unless specified otherwise.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface QuarterHourlyFlexConstraints extends FlexConstraints {
}
