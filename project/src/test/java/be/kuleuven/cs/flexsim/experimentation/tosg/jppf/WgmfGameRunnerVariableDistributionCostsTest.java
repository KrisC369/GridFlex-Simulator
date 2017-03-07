package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.DistributionGridCongestionSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.MultiHorizonErrorGenerator;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.ExperimentParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfGameParams;
import be.kuleuven.cs.flexsim.experimentation.tosg.WgmfInputParser;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.WindBasedInputData;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunnerVariableDistributionCostsTest {
    private static final String DISTRIBUTIONFILE = "windspeedDistributionsNormalized.csv";
    private static final String DATAFILE = "test.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices_short.csv";
    private static final String DAM_COLUMN = "damhp";
    private static final String DAMPRICES_DAILY = "dailyDayAheadPrices.csv";
    private static final String DB_PATH = "persistence/testDB.db";
    private static final int HORIZON = 7;

    private ExperimentParams experimentParams;
    private WgmfGameRunnerVariableDistributionCosts runner;

    @Before
    public void setUp() throws Exception {
        experimentParams = getParams("DUMMY");
        runner = new WgmfGameRunnerVariableDistributionCosts(experimentParams);
    }

    @Test
    @Ignore
    public void main() throws Exception {
        //        WgmfGameRunnerVariableDistributionCosts.main(getArgLine("OPTA"));
        WgmfMultiJobGameRunnerVariableDistributionCosts.main(getArgLine("OPTA"));

    }

    public static ExperimentParams getParams(String solver) {
        return WgmfInputParser.parseInputAndExec(getArgLine(solver));
    }

    public static String[] getArgLine(String solver) {
        //        return new String[] {
        //                "-n", "2", "-r", "1", "-s", solver, "-m", "LOCAL", "-p1start", "35.4",
        // "-p1step",
        //                "10", "-p1end", "45.5", "-dIdx", "1", "-pIdx", "1" };
        return new String[] {
                "-n", "2", "-r", "1", "-s", solver, "-c", "uw", "-m", "LOCAL", "-p1start", "35.4",
                "-p1step",
                "10", "-p1end", "45.5", "-pIdx", "1" };
    }

    public static WgmfGameParams loadTestResources(ExperimentParams expP) {
        return loadTestResources(expP, IMBAL, DATAFILE, "test", "test", HORIZON);
    }

    public static WgmfGameParams loadTestResources(ExperimentParams expP, String imbal,
            String datafile, String congColumn, String currColumn, int horizon) {
        try {
            WindBasedInputData dataIn = WindBasedInputData
                    .loadFromResource(datafile, congColumn, currColumn);
            TurbineSpecification specs = TurbineSpecification.loadFromResource(SPECFILE);
            ImbalancePriceInputData imbalIn = ImbalancePriceInputData.loadFromResource(imbal);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV(DISTRIBUTIONFILE);
            DayAheadPriceProfile dayAheadPriceProfile = DayAheadPriceProfile
                    .extrapolateFromHourlyOneDayData(DAMPRICES_DAILY, DAM_COLUMN, horizon);
            return WgmfGameParams
                    .create(dataIn, new WgmfSolverFactory(expP.getSolver(), DB_PATH,
                                    expP.getCachingEnabled(), expP.getUpdateCacheEnabled(),
                                    false), specs,
                            distribution, imbalIn, dayAheadPriceProfile);
        } catch (IOException e) {
            throw new IllegalStateException("One of the resources could not be loaded.", e);
        }
    }

    @Test
    @Ignore
    public void testGurobiCPLEXEquality() {
        experimentParams = getParams("CPLEX");
        //        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams,
        // "imbalance_prices.csv",
        //                "4kwartOpEnNeer.csv", "verlies aan energie", "startprofiel+extra", 365);
        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams);
        DayAheadPriceProfile dayAheadPriceData = wgmfGameParams.getDayAheadPriceData();
        MultiHorizonErrorGenerator multiHorizonErrorGenerator = new MultiHorizonErrorGenerator(
                1000, wgmfGameParams.getDistribution());

        PortfolioBalanceSolver portfolioBalanceSolver = new PortfolioBalanceSolver(
                wgmfGameParams.getFactory(),
                wgmfGameParams.getInputData().getCableCurrentProfile(), wgmfGameParams
                .getImbalancePriceData()
                .getNetRegulatedVolumeProfile(),
                wgmfGameParams.getImbalancePriceData()
                        .getPositiveImbalancePriceProfile(), wgmfGameParams.getSpecs(),
                multiHorizonErrorGenerator, dayAheadPriceData);
        HourlyFlexConstraints constr = HourlyFlexConstraints.builder().activationDuration(1)
                .interActivationTime(2).maximumActivations(4).build();
        portfolioBalanceSolver.registerFlexProvider(new FlexProvider(200, constr));
        portfolioBalanceSolver.registerFlexProvider(new FlexProvider(500, constr));
        portfolioBalanceSolver.solve();
        SolutionResults solutionCPL = portfolioBalanceSolver.getSolution();
        double compCPL = portfolioBalanceSolver.getFlexibilityProviders().stream()
                .mapToDouble(fp -> fp.getMonetaryCompensationValue()).sum();

        System.out.println("ObjectiveValue CPL:" + solutionCPL.getObjectiveValue());

        experimentParams = getParams("GUROBI");
        //        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams,
        // "imbalance_prices.csv",
        //                "4kwartOpEnNeer.csv", "verlies aan energie", "startprofiel+extra", 365);
        wgmfGameParams = loadTestResources(experimentParams);
        dayAheadPriceData = wgmfGameParams.getDayAheadPriceData();
        multiHorizonErrorGenerator = new MultiHorizonErrorGenerator(
                1000, wgmfGameParams.getDistribution());

        portfolioBalanceSolver = new PortfolioBalanceSolver(
                wgmfGameParams.getFactory(),
                wgmfGameParams.getInputData().getCableCurrentProfile(), wgmfGameParams
                .getImbalancePriceData()
                .getNetRegulatedVolumeProfile(),
                wgmfGameParams.getImbalancePriceData()
                        .getPositiveImbalancePriceProfile(), wgmfGameParams.getSpecs(),
                multiHorizonErrorGenerator, dayAheadPriceData);
        constr = HourlyFlexConstraints.builder().activationDuration(1)
                .interActivationTime(2).maximumActivations(4).build();
        portfolioBalanceSolver.registerFlexProvider(new FlexProvider(200, constr));
        portfolioBalanceSolver.registerFlexProvider(new FlexProvider(500, constr));
        portfolioBalanceSolver.solve();
        SolutionResults solutiongGRB = portfolioBalanceSolver.getSolution();
        double compGRB = portfolioBalanceSolver.getFlexibilityProviders().stream()
                .mapToDouble(fp -> fp.getMonetaryCompensationValue()).sum();

        System.out.println(
                "ObjectiveValue GRB:" + solutiongGRB.getObjectiveValue() + " , CompensationSum: "
                        + compGRB);

        assertEquals(solutionCPL.getObjectiveValue(), solutiongGRB.getObjectiveValue(), 1.0);
        assertEquals(compCPL, compGRB, 1.0);
    }

    @Test
    @Ignore
    public void testHeuristicCachingEquality() {
    }

    @Test
    @Ignore
    public void testGurobiCPLEXEquality2() {
        experimentParams = getParams("CPLEX");
        //        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams,
        // "imbalance_prices.csv",
        //                "4kwartOpEnNeer.csv", "verlies aan energie", "startprofiel+extra", 365);
        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams);
        DayAheadPriceProfile dayAheadPriceData = wgmfGameParams.getDayAheadPriceData();
        MultiHorizonErrorGenerator multiHorizonErrorGenerator = new MultiHorizonErrorGenerator(
                1000, wgmfGameParams.getDistribution());

        DistributionGridCongestionSolver DistributionGridCongestionSolver = new
                DistributionGridCongestionSolver(
                wgmfGameParams.getFactory(), wgmfGameParams.getInputData().getCongestionProfile());
        HourlyFlexConstraints constr = HourlyFlexConstraints.builder().activationDuration(1)
                .interActivationTime(2).maximumActivations(4).build();
        DistributionGridCongestionSolver.registerFlexProvider(new FlexProvider(200, constr));
        DistributionGridCongestionSolver.registerFlexProvider(new FlexProvider(500, constr));
        DistributionGridCongestionSolver.solve();
        SolutionResults solutionCPL = DistributionGridCongestionSolver.getSolution();
        double compCPL = DistributionGridCongestionSolver.getFlexibilityProviders().stream()
                .mapToDouble(fp -> fp.getMonetaryCompensationValue()).sum();

        System.out.println(
                "ObjectiveValue CPL:" + solutionCPL.getObjectiveValue() + "; CompensationSum: "
                        + compCPL);

        experimentParams = getParams("GUROBI");
        //        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams,
        // "imbalance_prices.csv",
        //                "4kwartOpEnNeer.csv", "verlies aan energie", "startprofiel+extra", 365);
        wgmfGameParams = loadTestResources(experimentParams);
        dayAheadPriceData = wgmfGameParams.getDayAheadPriceData();
        multiHorizonErrorGenerator = new MultiHorizonErrorGenerator(
                1000, wgmfGameParams.getDistribution());

        DistributionGridCongestionSolver = new
                DistributionGridCongestionSolver(
                wgmfGameParams.getFactory(), wgmfGameParams.getInputData().getCongestionProfile());
        constr = HourlyFlexConstraints.builder().activationDuration(1)
                .interActivationTime(2).maximumActivations(4).build();
        DistributionGridCongestionSolver.registerFlexProvider(new FlexProvider(200, constr));
        DistributionGridCongestionSolver.registerFlexProvider(new FlexProvider(500, constr));
        DistributionGridCongestionSolver.solve();
        SolutionResults solutiongGRB = DistributionGridCongestionSolver.getSolution();
        double compGRB = DistributionGridCongestionSolver.getFlexibilityProviders().stream()
                .mapToDouble(fp -> fp.getMonetaryCompensationValue()).sum();

        System.out.println(
                "ObjectiveValue GRB:" + solutiongGRB.getObjectiveValue() + "; CompensationSum: "
                        + compGRB);

        assertEquals(solutionCPL.getObjectiveValue(), solutiongGRB.getObjectiveValue(), 1.0);
        assertEquals(compCPL, compGRB, 1.0);
    }

    @Test
    public void testAbstractRunnerParseDistributionFileString() {
        String string = AbstractWgmfGameRunner
                .parseDataFileName(experimentParams.getWindErrorProfileIndex(), "testString*.csv");
        assertEquals("testString.csv", string);
        ExperimentParams p = mock(ExperimentParams.class);
        when(p.getWindErrorProfileIndex()).thenReturn(3);
        when(p.getCurrentDataProfileIndex()).thenReturn(3);
        String string2 = AbstractWgmfGameRunner
                .parseDataFileName(p.getCurrentDataProfileIndex(), "testString*.csv");
        assertEquals("testString[3].csv", string2);
    }
}