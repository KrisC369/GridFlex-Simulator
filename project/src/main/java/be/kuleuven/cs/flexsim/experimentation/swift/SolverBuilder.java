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

    public AbstractCongestionSolver getSolver(CongestionProfile profile, int n);

}
