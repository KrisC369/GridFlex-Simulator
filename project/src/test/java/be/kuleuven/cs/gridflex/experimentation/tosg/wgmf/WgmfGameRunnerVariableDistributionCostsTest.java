package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.AbstractFlexAllocationSolver;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.DistributionGridCongestionSolver;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.MultiHorizonNormalErrorGenerator;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.gridflex.domain.util.Payment;
import be.kuleuven.cs.gridflex.domain.util.data.DoublePowerCapabilityBand;
import be.kuleuven.cs.gridflex.domain.util.data.PowerForecastMultiHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import be.kuleuven.cs.gridflex.domain.util.data.WindSpeedForecastMultiHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.WindBasedInputData;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.SerializationUtils.pickle;
import static be.kuleuven.cs.gridflex.experimentation.tosg.wgmf.SerializationUtils.unpickle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfGameRunnerVariableDistributionCostsTest {
    static final double TO_POWER = 1.73 * 15.6;
    static final double CONVERSION = 1.5d;
    static final double SLOTS_PER_HOUR = 4;
    private static final String DISTRIBUTIONFILE = "windspeedDistributionsNormalized.csv";
    private static final String POWERDISTRIBUTION = "powerDistributionsTestFile.csv";
    private static final String DATAFILE = "test.csv";
    private static final String SPECFILE = "specs_enercon_e101-e1.csv";
    private static final String IMBAL = "imbalance_prices_short.csv";
    private static final String DAM_COLUMN = "damhp";
    private static final String DAMPRICES_DAILY = "dailyDayAheadPrices.csv";
    private static final String DB_PATH = "persistenceData/testDB.db";
    private static final int HORIZON = 7;
    private static final String DB_WRITE_FILE_LOCATION = "persistenceData/write/testDB.db";

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

    private static String[] getArgLine(String solver) {
        //        return new String[] {
        //                "-n", "2", "-r", "1", "-s", solver, "-m", "LOCAL", "-p1start", "35.4",
        // "-p1step",
        //                "10", "-p1end", "45.5", "-dIdx", "1", "-pIdx", "1" };
        return new String[] {
                "-n", "2", "-r", "1", "-s", solver, "-c", "ue", "-m", "LOCAL", "-p1start", "15.5",
                "-p1step",
                "10", "-p1end", "16.5", "-pIdx", "1", "-distribution", "CAUCHY", "-flexIA", "12",
                "-flexDUR", "2", "-flexCOUNT", "40" };
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
            WindSpeedForecastMultiHorizonErrorDistribution windDist =
                    WindSpeedForecastMultiHorizonErrorDistribution
                            .loadFromCSV(DISTRIBUTIONFILE);
            PowerForecastMultiHorizonErrorDistribution powerDist =
                    PowerForecastMultiHorizonErrorDistribution
                            .loadFromCSV(POWERDISTRIBUTION);
            DayAheadPriceProfile dayAheadPriceProfile = DayAheadPriceProfile
                    .extrapolateFromHourlyOneDayData(DAMPRICES_DAILY, DAM_COLUMN, horizon);

            WgmfMemContextFactory memContext = new WgmfMemContextFactory(expP.isCachingEnabled(),
                    expP.isCacheExistenceEnsured(), DB_PATH, DB_WRITE_FILE_LOCATION);
            return WgmfGameParams
                    .create(dataIn,
                            new WgmfSolverFactory(expP.getSolver(), expP.isUpdateCacheEnabled(),
                                    memContext), specs, windDist, powerDist, imbalIn,
                            dayAheadPriceProfile, expP.getDistribution(),
                            expP.getActivationConstraints());
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
        MultiHorizonNormalErrorGenerator multiHorizonNormalErrorGenerator = new
                MultiHorizonNormalErrorGenerator(
                1000, wgmfGameParams.getWindSpeedErrorDistributions());

        PortfolioBalanceSolver portfolioBalanceSolver = new PortfolioBalanceSolver(
                wgmfGameParams.getFactory(),
                wgmfGameParams.toSolverInputData(1000));
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
        multiHorizonNormalErrorGenerator = new MultiHorizonNormalErrorGenerator(
                1000, wgmfGameParams.getWindSpeedErrorDistributions());

        portfolioBalanceSolver = new PortfolioBalanceSolver(
                wgmfGameParams.getFactory(),
                wgmfGameParams.toSolverInputData(1000));
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
        MultiHorizonNormalErrorGenerator multiHorizonNormalErrorGenerator = new
                MultiHorizonNormalErrorGenerator(
                1000, wgmfGameParams.getWindSpeedErrorDistributions());

        AbstractFlexAllocationSolver DistributionGridCongestionSolver = new
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

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        byte[] pickle1 = pickle(new WgmfMemContextFactory(true, true, "one", "two"));
        WgmfMemContextFactory unpickle = unpickle(pickle1, WgmfMemContextFactory.class);
        assertTrue(unpickle != null);
    }

    @Test
    @Ignore
    public void testQuantifyNegativePayoffRatioPB() {

        final double[] positivePayment = { 0 };
        final double[] negativePayment = { 0 };
        experimentParams = getParams("OPTA");
        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams,
                "be/kuleuven/cs/gridflex/experimentation/data/imbalance_prices.csv",
                "be/kuleuven/cs/gridflex/experimentation/data/currentAndCongestionProfile[0].csv",
                "verlies aan energie", "startprofiel+extra", 365);

        PortfolioBalanceSolver portfolioBalanceSolver = new PortfolioBalanceSolver(
                wgmfGameParams.getFactory(),
                wgmfGameParams.toSolverInputData(1000),
                PortfolioBalanceSolver.ProfileConversionStrategy.POWER_ERROR_BASED);

        portfolioBalanceSolver.registerFlexProvider(
                new PseudoFlexProvider(negativePayment, positivePayment, 200));
        portfolioBalanceSolver.registerFlexProvider(
                new PseudoFlexProvider(negativePayment, positivePayment, 500));
        portfolioBalanceSolver.solve();
        SolutionResults solutionCPL = portfolioBalanceSolver.getSolution();
        double compCPL = portfolioBalanceSolver.getFlexibilityProviders().stream()
                .mapToDouble(fp -> fp.getMonetaryCompensationValue()).sum();

        System.out.println("ObjectiveValue Opta:" + solutionCPL.getObjectiveValue());
        double total = positivePayment[0] + negativePayment[0];
        double posRatio = positivePayment[0] / total;
        double negRatio = negativePayment[0] / total;
        System.out
                .println("Pos/Neg payment ratio: " + positivePayment[0] + "/" + negativePayment[0]);
        System.out.println("Total payment: " + total);
        System.out.println("Positive payment ratio: " + posRatio);
        System.out.println("Negative payment ratio: " + negRatio);
        assertEquals(0, negRatio, 0);
    }

    @Test
    @Ignore
    public void testQuantifyNegativePayoffRatioDGC() {

        final double[] positivePayment = { 0 };
        final double[] negativePayment = { 0 };
        experimentParams = getParams("OPTA");
        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams,
                "be/kuleuven/cs/gridflex/experimentation/data/imbalance_prices.csv",
                "be/kuleuven/cs/gridflex/experimentation/data/currentAndCongestionProfile[0].csv",
                "verlies aan energie", "startprofiel+extra", 365);
        //        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams);
        DayAheadPriceProfile dayAheadPriceData = wgmfGameParams.getDayAheadPriceData();
        MultiHorizonNormalErrorGenerator multiHorizonNormalErrorGenerator = new
                MultiHorizonNormalErrorGenerator(
                1000, wgmfGameParams.getWindSpeedErrorDistributions());

        DistributionGridCongestionSolver dgcSolver = new
                DistributionGridCongestionSolver(
                wgmfGameParams.getFactory(),
                wgmfGameParams.getInputData().getCongestionProfile());

        dgcSolver.registerFlexProvider(
                new PseudoFlexProvider(negativePayment, positivePayment, 200));
        dgcSolver.registerFlexProvider(
                new PseudoFlexProvider(negativePayment, positivePayment, 500));
        dgcSolver.solve();
        SolutionResults solutionCPL = dgcSolver.getSolution();
        double compCPL = dgcSolver.getFlexibilityProviders().stream()
                .mapToDouble(fp -> fp.getMonetaryCompensationValue()).sum();

        System.out.println("ObjectiveValue Opta:" + solutionCPL.getObjectiveValue());
        double total = positivePayment[0] + negativePayment[0];
        double posRatio = positivePayment[0] / total;
        double negRatio = negativePayment[0] / total;
        System.out
                .println("Pos/Neg payment ratio: " + positivePayment[0] + "/" + negativePayment[0]);
        System.out.println("Total payment: " + total);
        System.out.println("Positive payment ratio: " + posRatio);
        System.out.println("Negative payment ratio: " + negRatio);
        assertEquals(0, negRatio, 0);
    }

    @Test
    @Ignore
    public void testPortfolioBalanceProfileTransform() {

        experimentParams = getParams("OPTA");
        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams,
                "be/kuleuven/cs/gridflex/experimentation/data/imbalance_prices.csv",
                "be/kuleuven/cs/gridflex/experimentation/data/currentAndCongestionProfile[0].csv",
                "verlies aan energie", "startprofiel+extra", 365);

        PortfolioBalanceSolver portfolioBalanceSolver = new PortfolioBalanceSolver(
                wgmfGameParams.getFactory(),
                wgmfGameParams.toSolverInputData(1000),
                PortfolioBalanceSolver.ProfileConversionStrategy.POWER_ERROR_BASED);

        portfolioBalanceSolver
                .registerFlexProvider(new FlexProvider(200, HourlyFlexConstraints.R3DP));
        portfolioBalanceSolver
                .registerFlexProvider(new FlexProvider(500, HourlyFlexConstraints.R3DP));
        TimeSeries ts = portfolioBalanceSolver
                .getCongestionVolumeToResolve();
        System.out.println(ts.values());
        System.out.println(wgmfGameParams.getInputData().getCableCurrentProfile()
                .transform(p -> (p / CONVERSION) * TO_POWER)
                .transform(p -> p * CONVERSION / SLOTS_PER_HOUR).values());
        //        portfolioBalanceSolver.solve();
    }

    @Test
    @Ignore
    public void testDistributionGridCongestionProfileTransform() {

        experimentParams = getParams("OPTA");
        WgmfGameParams wgmfGameParams = loadTestResources(experimentParams,
                "be/kuleuven/cs/gridflex/experimentation/data/imbalance_prices.csv",
                "be/kuleuven/cs/gridflex/experimentation/data/currentAndCongestionProfile[0].csv",
                "verlies aan energie", "startprofiel+extra", 365);

        DistributionGridCongestionSolver dgcSolver = new
                DistributionGridCongestionSolver(
                wgmfGameParams.getFactory(),
                wgmfGameParams.getInputData().getCongestionProfile());

        dgcSolver
                .registerFlexProvider(new FlexProvider(200, HourlyFlexConstraints.R3DP));
        dgcSolver
                .registerFlexProvider(new FlexProvider(500, HourlyFlexConstraints.R3DP));
        TimeSeries ts = dgcSolver
                .getCongestionVolumeToResolve();
        System.out.println(ts.values());
        System.out.println(wgmfGameParams.getInputData().getCableCurrentProfile()
                .transform(p -> (p / CONVERSION) * TO_POWER)
                .transform(p -> p * CONVERSION / SLOTS_PER_HOUR).values());
        //        portfolioBalanceSolver.solve();
    }

    private static class PseudoFlexProvider implements FlexibilityProvider {
        private double compensation = 0;
        private final double[] negativePayment;
        private final double[] positivePayment;
        private final double rate;

        PseudoFlexProvider(double[] neg, double[] pos, double rate) {
            this.negativePayment = neg;
            this.positivePayment = pos;
            this.rate = rate;
            this.compensation = 0;
        }

        @Override
        public DoublePowerCapabilityBand getFlexibilityActivationRate() {
            return DoublePowerCapabilityBand.create(0, rate);
        }

        @Override
        public HourlyFlexConstraints getFlexibilityActivationConstraints() {
            return HourlyFlexConstraints.R3DP;
        }

        @Override
        public double getMonetaryCompensationValue() {
            return compensation;
        }

        @Override
        public void registerActivation(FlexActivation activation, Payment payment) {
            compensation += payment.getMonetaryAmount();
            if (payment.getMonetaryAmount() < 0) {
                negativePayment[0] += -payment.getMonetaryAmount();
            } else {
                positivePayment[0] += payment.getMonetaryAmount();
            }
        }
    }
}