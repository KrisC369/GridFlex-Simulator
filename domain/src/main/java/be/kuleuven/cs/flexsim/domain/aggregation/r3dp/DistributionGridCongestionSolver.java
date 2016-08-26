package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DistributionGridCongestionSolver extends FlexibilityUtiliser<SolutionResults> {

    private CongestionProfile congestion;
    @Nullable
    private SolutionResults results;

    public DistributionGridCongestionSolver(AbstractSolverFactory<SolutionResults> fac,
            CongestionProfile c) {
        super(fac);
        congestion = c;
    }

    @Override
    protected Solver<SolutionResults> configureSolver() {
        return getSolverFactory().createSolver(new FlexAllocProblemContext() {
            @Override
            public Iterable<FlexibilityProvider> getProviders() {
                return Collections.unmodifiableSet(getFlexibilityProviders());
            }

            @Override
            public CongestionProfile getEnergyProfileToMinimizeWithFlex() {
                return congestion;
            }
        });
    }

    @Override
    protected void performSolveStep(Solver<SolutionResults> s) {
        s.solve();
        this.results = s.getSolution();
        processActivations(results);
    }

    private void processActivations(@Nullable SolutionResults results) {
        for (FlexibilityProvider p : getFlexibilityProviders()) {
            processActivationsFor(p, results.getAllocationMaps().get(p));
        }
    }

    private void processActivationsFor(FlexibilityProvider p, List<Boolean> booleen) {
        for (int i = 0; i < booleen.size(); i++) {
            //TODO convert activation list to activation objects.
        }
    }

    @Override
    protected SolutionResults getResult() {
        return results;
    }
}
