package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolver extends AbstractFlexibilityUtiliser<SolutionResults> {
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            CongestionProfile c) {
        super(fac);
    }

    @Override
    protected Solver configureSolver() {
        return null;
    }

    @Override
    protected void performSolveStep() {

    }

    @Override
    protected SolutionResults getResult() {
        return null;
    }
}
