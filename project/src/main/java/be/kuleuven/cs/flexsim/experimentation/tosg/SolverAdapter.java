package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;

/**
 * Adapt solution instances from one type to another.
 *
 * @param <F> The result type to convert from.
 * @param <T> The result type to convert to.
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class SolverAdapter<F, T> implements Solver<T> {
    private Solver<F> target;

    /**
     * Default constructor.
     *
     * @param from The solver type to adapt.
     */
    public SolverAdapter(Solver<F> from) {
        this.target = from;
    }

    @Override
    public void solve() {
        target.solve();
    }

    @Override
    public T getSolution() {
        return adaptResult(target.getSolution());
    }

    /**
     * Perform the actual adaptation.
     *
     * @param solution The solution result to adapt.
     * @return The actual T type solution result.
     */
    public abstract T adaptResult(F solution);
}
