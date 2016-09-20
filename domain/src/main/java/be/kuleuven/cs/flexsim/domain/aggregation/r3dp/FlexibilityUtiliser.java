package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * @param R the result type.
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class FlexibilityUtiliser<R extends SolutionResults> {
    private final Set<FlexibilityProvider> providers;
    private boolean solutionReady;
    private final AbstractSolverFactory<R> solverFactory;

    FlexibilityUtiliser(AbstractSolverFactory<R> solver) {
        providers = Sets.newLinkedHashSet();
        solutionReady = false;
        solverFactory = solver;
    }

    public final void registerFlexProvider(FlexibilityProvider p1) {
        providers.add(p1);
    }

    public void solve() {
        Solver<R> s = configureSolver();
        performSolveStep(s);
        markSolved();
    }

    protected abstract Solver<R> configureSolver();

    private void markSolved() {
        solutionReady = true;
    }

    protected abstract void performSolveStep(Solver<R> s);

    public R getSolution() {
        checkSolutionReady();
        return getResult();
    }

    private void checkSolutionReady() {
        if (!solutionReady) {
            throw new IllegalStateException(
                    "Call the solve-method for this instance first before querying the results.");
        }
    }

    protected final AbstractSolverFactory<R> getSolverFactory() {
        return solverFactory;
    }

    protected abstract R getResult();

    public Set<FlexibilityProvider> getFlexibilityProviders() {
        return Collections.unmodifiableSet(providers);
    }
}
