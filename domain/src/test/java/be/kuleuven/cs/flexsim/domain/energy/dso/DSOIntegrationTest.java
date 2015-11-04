package be.kuleuven.cs.flexsim.domain.energy.dso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;

@RunWith(MockitoJUnitRunner.class)
public class DSOIntegrationTest {
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;

    private static String column = "test";
    private static String file = "test.csv";
    private AbstractCongestionSolver congestionSolver;

    private CongestionProfile congestionProfile;

    private DSMPartner dsm1;
    private DSMPartner dsm2;

    private Simulator sim;

    @Before
    public void setUp() throws Exception {
        try {
            congestionProfile = (CongestionProfile) CongestionProfile
                    .createFromCSV(file, column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CooperativeCongestionSolver(congestionProfile,
                8);
        dsm1 = new DSMPartner(0, 48, 8, 100, 1);
        dsm2 = new DSMPartner(0, 48, 8, 50, 1);
        // when(congestionProfile.value(anyInt())).thenReturn(175.0);
        // when(congestionProfile.values()).thenReturn(new double[4 * 24 *
        // 365]);

        sim = Simulator.createSimulator(500 - 1);
        sim.register(congestionSolver);
    }

    @Test
    public void testNoActivation() {
        congestionSolver = new CooperativeCongestionSolver(congestionProfile,
                8);
        dsm1 = new DSMPartner(0, 48, 8, 100, 1);
        dsm2 = new DSMPartner(0, 48, 8, 50, 1);
        register();
        sim.start();
        assertEquals(0, congestionSolver.getTotalRemediedCongestion(), 0);

    }

    private void register() {
        congestionSolver.registerDSMPartner(dsm1);
        congestionSolver.registerDSMPartner(dsm2);
        sim.register(congestionSolver);
    }

    @Test
    public void testPosActivation() {
        congestionSolver = new CooperativeCongestionSolver(congestionProfile, 8,
                100);
        dsm1 = new DSMPartner(40, 48, 8, 100, 1);
        dsm2 = new DSMPartner(40, 48, 8, 50, 1);
        register();
        sim.start();
        // assertNotEquals(0.0, congestionSolver.getTotalRemediedCongestion());
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm1.getCurrentActivations(),
                DSMPartner.R3DPMAX_ACTIVATIONS - 5);
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm2.getCurrentActivations(),
                DSMPartner.R3DPMAX_ACTIVATIONS - 5);
    }

    @Test
    public void testPosActivationNumberActivations() {
        int power = 100;
        congestionSolver = new CooperativeCongestionSolver(congestionProfile, 8,
                100);
        dsm1 = new DSMPartner(40, 48, 8, power, 1);
        register();
        sim.start();
        // assertNotEquals(0.0, congestionSolver.getTotalRemediedCongestion());
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm1.getCurrentActivations(),
                DSMPartner.R3DPMAX_ACTIVATIONS - 5);
        assertTrue(congestionSolver.getTotalRemediedCongestion() <= power
                * DSMPartner.R3DPMAX_ACTIVATIONS * 2);
    }

    @Test
    public void testCoopScenario1() {
        try {
            congestionProfile = (CongestionProfile) CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CooperativeCongestionSolver(congestionProfile,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 2000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 500, 1);
        sim = Simulator.createSimulator(25);
        register();
        sim.start();
        System.out.println();

    }

    @Test
    public void testCoopScenario2() {
        try {
            congestionProfile = (CongestionProfile) CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CooperativeCongestionSolver(congestionProfile,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 15000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 8000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurrentActivations() == 0);
    }

    @Test
    public void testCompScenario1() {
        try {
            congestionProfile = (CongestionProfile) CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CompetitiveCongestionSolver(congestionProfile,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 15000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 8000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() == 0);
        assertTrue(dsm1.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurtailment(10) == dsm2.getFlexPowerRate());
        assertEquals(0.01, getEfficiency(), 0.1);
    }

    @Test
    public void testCompScenario2() {
        try {
            congestionProfile = (CongestionProfile) CongestionProfile
                    .createFromCSV("smalltest.csv", column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CompetitiveCongestionSolver(congestionProfile,
                8);
        dsm1 = new DSMPartner(4, 10, 8, 8000, 1);
        dsm2 = new DSMPartner(4, 10, 8, 15000, 1);
        sim = Simulator.createSimulator(25);
        register();
        // sim.start();

        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.afterTick(1);
        congestionSolver.tick(1);
        congestionSolver.afterTick(1);
        assertTrue(dsm2.getCurrentActivations() > 0);
        assertTrue(dsm1.getCurrentActivations() == 0);
        assertTrue(dsm2.getCurtailment(10) == dsm1.getFlexPowerRate());
        assertEquals(0.01, getEfficiency(), 0.01);
    }

    @Test
    public void testScenario3() {
        try {
            congestionProfile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CompetitiveCongestionSolver(congestionProfile,
                8);
        List<DSMPartner> partners = Lists.newArrayList();
        GammaDistribution gd = new GammaDistribution(
                new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
        for (int i = 0; i < 185; i++) {
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        sim = Simulator.createSimulator(35040);
        sim.register(congestionSolver);
        sim.start();
        int totalActsComp = getTotalActs(partners);
        System.out.println("Coop: " + getTotalActs(partners));

        congestionSolver = new CooperativeCongestionSolver(congestionProfile,
                8);
        partners = Lists.newArrayList();
        gd = new GammaDistribution(new MersenneTwister(1312421l),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        for (int i = 0; i < 185; i++) {
            partners.add(new DSMPartner((int) gd.sample()));
            congestionSolver.registerDSMPartner(partners.get(i));
        }
        sim = Simulator.createSimulator(35040);
        sim.register(congestionSolver);
        sim.start();
        System.out.println("Comp: " + getTotalActs(partners));
        assertEquals(getTotalActs(partners), totalActsComp);
    }

    public double getEfficiency() {
        return congestionSolver.getTotalRemediedCongestion()
                / (getTotalPowerRates() * dsm1.getMaxActivations() * 2.0);
    }

    public int getTotalActs(List<DSMPartner> partners) {
        int sum = 0;
        for (DSMPartner p : partners) {
            sum += p.getCurrentActivations();
        }
        return sum;
    }

    private double getTotalPowerRates() {
        int sum = 0;
        sum += dsm1.getFlexPowerRate();
        sum += dsm2.getFlexPowerRate();
        return sum;
    }

}
