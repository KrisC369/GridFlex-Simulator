package be.kuleuven.cs.flexsim.solvers.memoization;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.persistence.MemoizationContext;
import be.kuleuven.cs.flexsim.solvers.Solvers;
import be.kuleuven.cs.flexsim.solvers.heuristic.solver.HeuristicSolverTest;
import be.kuleuven.cs.flexsim.solvers.optimal.AllocResults;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MemoizationDecoratorTest {
    private Solver<AllocResults> solver;
    private Solver<AllocResults> solver2;
    private Solver<AllocResults> decoratedSolver;
    private Solver<AllocResults> returningSolver;

    private AllocResults memoresults;

    private FlexAllocProblemContext context;
    private FlexibilityProvider first;
    private FlexibilityProvider second;
    private CongestionProfile profile;
    private Logger logger = org.slf4j.LoggerFactory.getLogger(HeuristicSolverTest.class);

    @Before
    public void setUp() throws IOException {
        this.profile = CongestionProfile.createFromCSV("smalltest.csv", "test");
        first = new FlexProvider(400,
                HourlyFlexConstraints.builder().maximumActivations(2).interActivationTime(1)
                        .activationDuration(0.5).build());
        second = new FlexProvider(560,
                HourlyFlexConstraints.builder().maximumActivations(2).interActivationTime(1)
                        .activationDuration(0.5).build());
        initSolvers();
    }

    private void initSolvers() {
        this.context = new FlexAllocProblemContext() {

            @Override
            public Iterable<FlexibilityProvider> getProviders() {
                return Lists.newArrayList(first, second);
            }

            @Override
            public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                return profile;
            }
        };
        Solver<AllocResults> t1 = Solvers.createHeuristicOptaplanner(context, true);
        Solver<AllocResults> t2 = Solvers.createHeuristicOptaplanner(context, true);
        solver = spy(t1);
        solver2 = spy(t2);

        SimpleMemoizationContext memo = new SimpleMemoizationContext();

        this.decoratedSolver = new MemoizationDecorator(solver, context, memo);
        this.returningSolver = new MemoizationDecorator(solver2, context, memo);

    }

    @Test
    public void solve() throws Exception {
        AllocResults solution1 = decoratedSolver.solve();

        AllocResults solution2 = returningSolver.solve();

        assertEquals(solution1, solution2);
        verify(solver, times(1)).solve();
        verify(solver2, times(0)).solve();
    }

    @Test
    public void getSolution() throws Exception {

    }

    void setArg(AllocResults results) {
        this.memoresults = results;
    }

    class SimpleMemoizationContext
            implements MemoizationContext<FlexAllocProblemContext, AllocResults> {

        private FlexAllocProblemContext context;
        private AllocResults cachedResults;

        @Override
        public void memoizeEntry(FlexAllocProblemContext entry, AllocResults result) {
            this.context = entry;
            this.cachedResults = result;
        }

        @Override
        public boolean hasResultFor(FlexAllocProblemContext entry) {
            return context != null;
        }

        @Override
        public AllocResults getMemoizedResultFor(FlexAllocProblemContext entry) {
            if (entry.equals(context)) {
                return cachedResults;
            } else {
                throw new IllegalStateException("No cached result here.");
            }
        }
    }
}