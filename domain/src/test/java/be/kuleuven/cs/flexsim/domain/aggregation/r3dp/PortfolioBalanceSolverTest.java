package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.CableCurrentProfile;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolverTest {

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
            c2 = CableCurrentProfile.createFromCSV("smalltest.csv", "test");
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testConvertProfiles() {
        CableCurrentProfile cableCurrentProfile2 = PortfolioBalanceSolver.convertProfile(c2, specs);
        assertEquals(cableCurrentProfile2.values(), c2.values());
    }

}