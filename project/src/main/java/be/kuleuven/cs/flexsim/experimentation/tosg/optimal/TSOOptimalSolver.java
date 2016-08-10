package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;

import java.util.Optional;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class TSOOptimalSolver extends AbstractOptimalSolver {
    public TSOOptimalSolver(final CongestionProfile c, final Solver solver) {

    }

    @Override
    protected void processResults(final Optional<MpResult> result) {

    }

    @Override
    public Solver getSolver() {
        return Solver.CPLEX;
    }

    @Override
    public MpProblem getProblem() {
        return new MpProblem();
    }

    @Override
    public AllocResults getResults() {
        return AllocResults.INFEASIBLE;
    }
}
