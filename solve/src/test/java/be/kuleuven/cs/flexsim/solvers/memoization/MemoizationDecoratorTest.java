package be.kuleuven.cs.flexsim.solvers.memoization;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.persistence.MapDBMemoizationContext;
import be.kuleuven.cs.flexsim.persistence.MemoizationContext;
import be.kuleuven.cs.flexsim.solvers.AllocResults;
import be.kuleuven.cs.flexsim.solvers.Solvers;
import be.kuleuven.cs.flexsim.solvers.heuristic.solver.HeuristicSolverTest;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import com.google.common.base.Supplier;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MemoizationDecoratorTest {
    private static final String DB_NAME = "SolveTest.db";
    private Solver<AllocResults> solver;
    private Solver<AllocResults> solver2;
    private Solver<AllocResults> decoratedSolver;
    private Solver<AllocResults> returningSolver;

    private MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> memo;
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
        initSolvers(false);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        MapDBMemoizationContext<String, String> target =
                MapDBMemoizationContext.builder().setFileName(DB_NAME).build();

        target.resetStore();
        MapDBMemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> target2 =
                MapDBMemoizationContext.builder().setFileName(DB_NAME).build();

        target.resetStore();
    }

    private void initSolvers(boolean realMemo) {
        this.context = new FlexAllocProblemContext() {

            @Override
            public Collection<FlexibilityProvider> getProviders() {
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
        if (realMemo) {
            memo = MapDBMemoizationContext.builder().setFileName(DB_NAME).ensureFileExists()
                    .build();

        } else {
            memo = new SimpleMemoizationContext();
        }
        this.decoratedSolver = new MemoizationDecorator(solver, context, () -> memo, true);
        this.returningSolver = new MemoizationDecorator(solver2, context, () -> memo, true);

    }

    @Test
    public void solve() throws Exception {
        doTest1();
    }

    private void doTest1() {
        AllocResults solution1 = decoratedSolver.solve();
        AllocResults solution2 = returningSolver.solve();

        assertEquals(solution1, solution2);
        verify(solver, times(1)).solve();
        verify(solver2, times(0)).solve();
    }

    @Test
    public void solveRealMemo() throws Exception {
        setUp();
        initSolvers(true);
        doTest1();
        ((MapDBMemoizationContext) memo).resetStore();
    }

    @Test
    public void getSolution() throws Exception {
    }

    @Test
    public void testObjectSerialization() throws Exception {
        initSolvers(true);
        MapDBMemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> target2 =
                MapDBMemoizationContext.builder().setFileName(DB_NAME).ensureFileExists().build();

        ListMultimap<FlexibilityProvider, Boolean> lmm = LinkedListMultimap.create();
        lmm.put(first, Boolean.TRUE);
        lmm.put(first, Boolean.FALSE);
        lmm.put(second, Boolean.TRUE);
        lmm.put(second, Boolean.FALSE);

        ImmutableSolverProblemContextView key = ImmutableSolverProblemContextView.from(context);
        AllocResultsView value = AllocResultsView.from(AllocResults.create(lmm, 342.0, 2342.3));
        target2.memoizeEntry(key, value);
        AllocResultsView memoizedResultFor = target2.getMemoizedResultFor(key);
        assertEquals(value, memoizedResultFor);
        ((MapDBMemoizationContext) memo).resetStore();
    }

    static class SimpleMemoizationContext
            implements MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> {

        private ImmutableSolverProblemContextView context;
        private AllocResultsView cachedResults;

        @Override
        public AllocResultsView testAndCall(ImmutableSolverProblemContextView entry,
                Supplier<AllocResultsView> calculationFu, boolean whatever) {
            if (context != null) {
                return cachedResults;
            }
            this.context = entry;
            this.cachedResults = calculationFu.get();
            return cachedResults;
        }
    }
}