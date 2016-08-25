package be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver;

/**
 * Generic solver interface.
 *
 * @param <T> The result solution type.
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface Solver<T> {
    /**
     * Solve the problem configured in the solver.
     */
    void solve();

    /**
     * Get the solution.
     *
     * @return the solution
     */
    T getSolution();
}
