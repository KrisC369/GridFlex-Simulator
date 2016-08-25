package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;

import java.util.Collections;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DistributionGridCongestionSolver extends AbstractFlexibilityUtiliser<SolutionResults> {

    private CongestionProfile congestion;

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
    protected void performSolveStep() {

    }

    @Override
    protected SolutionResults getResult() {
        return null;
    }
}
