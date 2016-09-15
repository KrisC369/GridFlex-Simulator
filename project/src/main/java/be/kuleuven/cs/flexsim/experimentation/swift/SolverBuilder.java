/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.util.data.AbstractTimeSeriesImplementation;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */@FunctionalInterface
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
    AbstractCongestionSolver getSolver(AbstractTimeSeriesImplementation profile, int n);

}
