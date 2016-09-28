package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.experimentation.tosg.SolutionResultAdapter;
import be.kuleuven.cs.flexsim.experimentation.tosg.SolverAdapter;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import be.kuleuven.cs.flexsim.solver.optimal.dso.DSOOptimalSolver;

import java.io.Serializable;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfSolverFactory implements AbstractSolverFactory<SolutionResults>, Serializable {
    private AbstractOptimalSolver.Solver type;

    WgmfSolverFactory(AbstractOptimalSolver.Solver type) {
        this.type = type;
    }

    @Override
    public Solver<SolutionResults> createSolver(FlexAllocProblemContext context) {
        return new SolverAdapter<AllocResults, SolutionResults>(
                new DSOOptimalSolver(context, type)) {

            @Override
            public SolutionResults adaptResult(AllocResults solution) {
                return new SolutionResultAdapter(solution).getResults();
            }
        };
    }
}
