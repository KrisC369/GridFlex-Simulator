package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import autovalue.shaded.com.google.common.common.collect.Sets;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;

import java.util.Collections;
import java.util.Set;

/**
 * @param R the result type.
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class AbstractFlexibilityUtiliser<R extends SolutionResults> {
    private final Set<FlexProvider> providers;
    private boolean solutionReady;
    private AbstractSolverFactory<R> solverFactory;

    AbstractFlexibilityUtiliser(AbstractSolverFactory<R> solver) {
        providers = Sets.newLinkedHashSet();
        solutionReady = false;
        solverFactory = solver;
    }

    public final void registerFlexProvider(FlexProvider p1) {
        providers.add(p1);
    }

    public void solve() {
        Solver<R> s = configureSolver();
        performSolveStep();
        markSolved();
    }

    protected abstract Solver<R> configureSolver();

    private void markSolved() {
        solutionReady = true;
    }

    protected abstract void performSolveStep();

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
