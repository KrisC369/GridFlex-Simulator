package be.kuleuven.cs.gridflex.domain.aggregation.r3dp;

import be.kuleuven.cs.gridflex.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.gridflex.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.PowerValuesProfile;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.fail;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolverTest {
    private static final double EPSILON = 0.001;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final long SEED = 1312421L;
    private CableCurrentProfile c2;
    private TurbineSpecification specs;
    private GammaDistribution gd;
    private PortfolioBalanceSolver solver;
    private MultiHorizonErrorGenerator generator;

    @Before
    public void setUp() throws Exception {
        gd = new GammaDistribution(new MersenneTwister(SEED),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        try {
            specs = TurbineSpecification.loadFromResource("specs_enercon_e101-e1.csv");
            c2 = CableCurrentProfile.createFromCSV("smalltest.csv", "test").transform(p -> p * 2);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV("windspeedDistributions.csv");
            this.generator = new MultiHorizonErrorGenerator(SEED, distribution);

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testApplicationOfError0s() throws IOException {
        ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                .loadFromCSV("windspeedDistributionsEmpty.csv");
        this.generator = new MultiHorizonErrorGenerator(SEED, distribution);

        CongestionProfile cableCurrentProfile2 = toWindAndBackWErrors(c2, specs);
        List<Double> expected = c2.transform(p -> p * TurbineProfileConvertor.TO_POWER / 4d)
                .values();
        List<Double> actual = cableCurrentProfile2.values();
        //printList(actual, expected);
        //printAvgDelta(actual, Collections.nCopies(c2.length(), 0d));
        assertEqualArrays(Collections.nCopies(c2.length(), 0d), actual);
    }

    private void printList(List<Double>... lists) {
        for (List<Double> l : lists) {
            LoggerFactory.getLogger(PortfolioBalanceSolverTest.class)
                    .info(l.toString());
        }
    }

    @Test
    public void testToWindAndBackProfiles() {
        PowerValuesProfile cableCurrentProfile2 = toWindAndBack(c2, specs);
        List<Double> expected = c2.transform(
                p -> p * TurbineProfileConvertor.TO_POWER / TurbineProfileConvertor.CONVERSION)
                .values();
        List<Double> actual = cableCurrentProfile2.values();
        //        assertEquals(expected, actual);
        //        printAvgDelta(expected, actual);
        assertEqualArrays(expected, actual);
    }

    @Test
    public void testToWindAndBackProfiles2() throws IOException {
        ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                .loadFromCSV("windspeedDistributionsEmpty.csv");
        this.generator = new MultiHorizonErrorGenerator(SEED, distribution);
        TurbineProfileConvertor t = new TurbineProfileConvertor(c2, specs, generator);
        CongestionProfile orig = t.getOriginalCongestionProfile();
        CongestionProfile cableCurrentProfile2 = t.getPredictionCongestionProfile();
        List<Double> expected = orig.values();
        List<Double> actual = cableCurrentProfile2.values();
        //        assertEquals(expected, actual);
        //        printAvgDelta(expected, actual);
        assertEqualArrays(expected, actual);
    }

    @Test
    public void testProcessActivations() {
    }

    private CongestionProfile toWindAndBackWErrors(CableCurrentProfile c2,
            TurbineSpecification specs) {
        TurbineProfileConvertor t = new TurbineProfileConvertor(c2, specs, generator);
        return t.convertProfileToImbalanceVolumes();
    }

    private PowerValuesProfile toWindAndBack(CableCurrentProfile c2, TurbineSpecification specs) {
        TurbineProfileConvertor t = new TurbineProfileConvertor(c2, specs, generator);
        return t.toPowerValues(t.toWindSpeed());
    }

    private void printAvgDelta(List<Double> expected, List<Double> actual) {
        long count = IntStream.range(0, expected.size())
                .filter(i -> notEqual(expected.get(i), actual.get(i)))
                .filter(i -> (Math.abs(expected.get(i) - actual.get(i)) > 0)).count();
        double avg = IntStream.range(0, expected.size())
                .filter(i -> notEqual(expected.get(i), actual.get(i))).filter(i -> i > 0)
                .mapToDouble(i -> Math.abs(expected.get(i) - actual.get(i))).sum() / count;
        LoggerFactory.getLogger(PortfolioBalanceSolverTest.class)
                .info("Avg Diff between profiles: " + avg);
    }

    public <R extends Number> void assertEqualArrays(List<R> first, List<R> second) {
        if (first.size() != second.size()) {
            fail();
        }
        if (IntStream.range(0, first.size()).filter(i -> notEqual(first.get(i), second.get(i)))
                .count() > 0) {
            fail();
        }
    }

    private <R extends Number> boolean notEqual(R r1, R r2) {
        return Math.abs(r2.doubleValue() - r1.doubleValue()) > EPSILON;
    }

}