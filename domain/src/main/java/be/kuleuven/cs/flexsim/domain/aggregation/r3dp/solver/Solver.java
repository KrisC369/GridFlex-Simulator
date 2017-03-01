package be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver;

/**
 * Generic solvers interface.
 *
 * @param <T> The result solution type.
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface Solver<T> {

    /**
     * Solve the problem configured in the solvers.
     * * @return the solution
     */
    T solve();
}
