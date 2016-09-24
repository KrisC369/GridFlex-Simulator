package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DistributionGridCongestionSolverTest {

    private static final double EPSILON = 0.001;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final long SEED = 1312421L;
    private CongestionProfile c2;
    private TurbineSpecification specs;
    private GammaDistribution gd;
    private WindErrorGenerator generator;
    private DistributionGridCongestionSolver solver;
    private ListMultimap<FlexibilityProvider, Boolean> toTest;

    @Before
    public void setUp() throws Exception {
        gd = new GammaDistribution(new MersenneTwister(SEED),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        try {
            specs = TurbineSpecification.loadFromResource("specs_enercon_e101-e1.csv");
            c2 = CongestionProfile.createFromCSV("smalltest.csv", "test").transform(p -> p * 2);
            ForecastHorizonErrorDistribution distribution = ForecastHorizonErrorDistribution
                    .loadFromCSV("windspeedDistributions.csv");
            this.generator = new WindErrorGenerator(SEED, distribution);
            //            c2 = CableCurrentProfile.createFromCSV("4kwartOpEnNeer.csv",
            // "startprofiel+extra");
            AbstractSolverFactory<SolutionResults> t = mock(AbstractSolverFactory.class);
            solver = new DistributionGridCongestionSolver(t, c2);
            toTest = ArrayListMultimap.create();
            List<Boolean> tt = Collections.nCopies(40, Boolean.TRUE);
            toTest.putAll(mock(FlexibilityProvider.class), tt);
            tt = Collections.nCopies(40, Boolean.TRUE);
            toTest.putAll(mock(FlexibilityProvider.class), tt);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConsolidateActivations() {
        List<Integer> integers = solver.consolidateActivations(toTest);
        long count = integers.stream().mapToInt(i -> i).sum();
        assertEquals(2 * 40, count, 0);
    }

}