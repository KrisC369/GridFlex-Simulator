package be.kuleuven.cs.gridflex.experimentation.tosg.regression;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.MultiHorizonErrorGenerator;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.ExperimentParams;
import be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.WgmfGameParams;
import be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.WgmfSolverFactory;
import be.kuleuven.cs.gridflex.persistence.MapDBMemoizationContext;
import be.kuleuven.cs.gridflex.persistence.MemoizationContext;
import be.kuleuven.cs.gridflex.solvers.Solvers;
import be.kuleuven.cs.gridflex.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.gridflex.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf
        .WgmfGameRunnerVariableDistributionCostsTest.getParams;
import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf
        .WgmfGameRunnerVariableDistributionCostsTest.loadTestResources;
import static org.junit.Assert.assertEquals;

/**
 * Optaplanner regression test. This test attempt to deserialize cached results and calculate
 * from there on.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class OptaSerializeRegression {
    private static final String DB_PATH = "persistenceData/regressionDB.db";
    private static final String DB_WRITE_FILE_LOCATION = "persistenceData/write/regressionDB.db";
    private static final double DELTA = 0.001;
    private ExperimentParams experimentParams;
    private static final double HARDCODED_RESULTS = 2203.3956414138675;
    private static final boolean UPDATE_RESULTS = false;

    @Before
    public void setUp() throws Exception {
        experimentParams = getParams("OPTA");
    }

    @Test
    public void testOptaPlannerWSerializationRegression() {
        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams);
        DayAheadPriceProfile dayAheadPriceData = wgmfGameParams.getDayAheadPriceData();
        MultiHorizonErrorGenerator multiHorizonErrorGenerator = new MultiHorizonErrorGenerator(
                1000, wgmfGameParams.getDistribution());

        Supplier<MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>>
                memContext2 = () -> new CacheResultOnlyMemoizationDecorator(
                MapDBMemoizationContext.builder().setFileName(DB_PATH)
                        .setDifferentWriteFilename(DB_WRITE_FILE_LOCATION).ensureFileExists
                        (true).appendHostnameToWriteFileName(true).build());

        WgmfSolverFactory factory = new WgmfSolverFactory(Solvers.TYPE.OPTA, UPDATE_RESULTS,
                memContext2);

        PortfolioBalanceSolver portfolioBalanceSolver = new PortfolioBalanceSolver(
                factory,
                wgmfGameParams.toSolverInputData(1000));
        HourlyFlexConstraints constr = HourlyFlexConstraints.builder().activationDuration(1)
                .interActivationTime(2).maximumActivations(4).build();
        portfolioBalanceSolver.registerFlexProvider(new FlexProvider(200, constr));
        portfolioBalanceSolver.registerFlexProvider(new FlexProvider(500, constr));
        portfolioBalanceSolver.solve();
        SolutionResults solutionOPTA = portfolioBalanceSolver.getSolution();

        System.out.println("ObjectiveValue:" + solutionOPTA.getObjectiveValue());
        assertEquals(HARDCODED_RESULTS, solutionOPTA.getObjectiveValue(), DELTA);

    }

    private class CacheResultOnlyMemoizationDecorator
            implements MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> {

        private MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> delegate;

        private CacheResultOnlyMemoizationDecorator(
                MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> delegate) {
            this.delegate = delegate;
        }

        @Override
        public AllocResultsView testAndCall(ImmutableSolverProblemContextView entry,
                com.google.common.base.Supplier<AllocResultsView> calculationFu,
                boolean updateCache) {
            return delegate.testAndCall(entry, () -> {
                throw new IllegalStateException(
                        "Should not need to calculate results with this decorator.");
            }, updateCache);
        }
    }

}
