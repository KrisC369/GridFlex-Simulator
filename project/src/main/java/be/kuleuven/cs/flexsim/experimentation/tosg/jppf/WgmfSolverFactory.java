package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.experimentation.tosg.adapters.SolutionResultAdapter;
import be.kuleuven.cs.flexsim.experimentation.tosg.adapters.SolverAdapter;
import be.kuleuven.cs.flexsim.solvers.Solvers;
import be.kuleuven.cs.flexsim.solvers.optimal.AllocResults;

import java.io.Serializable;

/**
 * A solvers factory for wgmf games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *///TODO move to outer package
public class WgmfSolverFactory implements AbstractSolverFactory<SolutionResults>, Serializable {
    private static final long serialVersionUID = -5851172788369007725L;
    private final Solvers.TYPE type;
    private final String filepath;
    private long defaultSeed = 0L;

    WgmfSolverFactory(Solvers.TYPE type, String filepath) {
        this.type = type;
        this.filepath = filepath;
    }

    public void setSeed(long seed) {
        this.defaultSeed = seed;
    }

    @Override
    public Solver<SolutionResults> createSolver(final FlexAllocProblemContext context) {
        return new SolverAdapter<AllocResults, SolutionResults>(
                type.getCachingInstance(new FlexAllocProblemContext() {
                    @Override
                    public Iterable<FlexibilityProvider> getProviders() {
                        return context.getProviders();
                    }

                    @Override
                    public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                        return context.getEnergyProfileToMinimizeWithFlex();
                    }

                    @Override
                    public long getSeedValue() {
                        return defaultSeed;
                    }
                }, filepath)) {

            @Override
            public SolutionResults adaptResult(AllocResults solution) {
                return new SolutionResultAdapter(solution).getResults();
            }
        };
    }
}
