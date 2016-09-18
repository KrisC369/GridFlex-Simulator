package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.CableCurrentProfile;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
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

    @Before
    public void setUp() throws Exception {
        gd = new GammaDistribution(new MersenneTwister(SEED),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        try {
            specs = TurbineSpecification.loadFromResource("specs_enercon_e101-e1.csv");
            c2 = CableCurrentProfile.createFromCSV("smalltest.csv", "test").transform(p -> p * 2);
            //            c2 = CableCurrentProfile.createFromCSV("4kwartOpEnNeer.csv",
            // "startprofiel+extra");

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testConvertProfiles() {
        CableCurrentProfile cableCurrentProfile2 = PortfolioBalanceSolver.convertProfile(c2, specs);
        List<Double> expected = c2.transform(p -> p * TurbineProfileConvertor.TO_POWER).values();
        List<Double> actual = cableCurrentProfile2.values();
        //        assertEquals(expected, actual);
        //        printAvgDelta(expected, actual);
        assertEqualArrays(expected, actual);
    }

    private void printAvgDelta(List<Double> expected, List<Double> actual) {
        long count = IntStream.range(0, expected.size())
                .filter(i -> notEqual(expected.get(i), actual.get(i)))
                .count();
        double avg = IntStream.range(0, expected.size())
                .map(i -> (int) (100 * Math.abs(expected.get(i) - actual.get(i))))
                .sum() / (100d * count);
        System.out.println(avg);
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