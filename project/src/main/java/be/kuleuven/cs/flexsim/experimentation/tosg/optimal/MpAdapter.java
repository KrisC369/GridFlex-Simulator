package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpVariable;

import java.util.List;

/**
 * /**
 * An adapter for converting the FlexConstraint interface to an interface that provides solver
 * constraints and variables.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
interface MpAdapter {
    /**
     * Returns decision variables generated for this provider.
     *
     * @return a list of Decision variables.
     */
    List<MpVariable> getDVars();

    /**
     * Returns constraints generated for this provider.
     *
     * @return a list of constraints.
     */
    List<MpConstraint> getConstraints();
}
