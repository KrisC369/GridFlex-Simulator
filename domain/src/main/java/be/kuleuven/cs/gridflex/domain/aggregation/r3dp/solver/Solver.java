package be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver;

/**
 * Generic solvers interface.
 *
 * @param <T> The result solution type.
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */@FunctionalInterface
public interface Solver<T> {

    /**
     * Solve the problem configured in the solvers.
     * * @return the solution
     */
    T solve();
}
