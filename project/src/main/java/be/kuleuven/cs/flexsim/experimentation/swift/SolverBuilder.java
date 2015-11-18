/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import be.kuleuven.cs.flexsim.domain.energy.dso.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface SolverBuilder {

    /**
     * Abstract factory creator method for returning a specific solver.
     * 
     * @param profile
     *            The congestion profile to be used.
     * @param n
     *            The forecast horizon.
     * @return A congestion solver instance.
     */
    public AbstractCongestionSolver getSolver(CongestionProfile profile, int n);

}
