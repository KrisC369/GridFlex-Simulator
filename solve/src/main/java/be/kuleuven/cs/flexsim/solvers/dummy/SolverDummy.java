package be.kuleuven.cs.flexsim.solvers.dummy;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.solvers.optimal.AllocResults;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class SolverDummy implements Solver<AllocResults> {

    @Override
    public void solve() {
    }

    @Override
    public AllocResults getSolution() {
        return AllocResults.INFEASIBLE;
    }

}
