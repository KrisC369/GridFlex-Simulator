package be.kuleuven.cs.flexsim.domain.energy.dso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;

@RunWith(MockitoJUnitRunner.class)
public class DSOIntegrationTest {

    private CongestionSolver congestionSolver;
    @Mock
    private CongestionProfile congestionProfile;

    private DSMPartner dsm1;
    private DSMPartner dsm2;

    private Simulator sim;

    @Before
    public void setUp() throws Exception {
        congestionSolver = new CongestionSolver(congestionProfile, 8);
        dsm1 = new DSMPartner(0, 48, 8, 100, -1);
        dsm2 = new DSMPartner(0, 48, 8, 50, -1);
        when(congestionProfile.value(anyInt())).thenReturn(175.0);
        when(congestionProfile.values()).thenReturn(new double[4 * 24 * 365]);

        sim = Simulator.createSimulator(4 * 24 * 365);
        sim.register(congestionSolver);
    }

    @Test
    public void testNoActivation() {
        congestionSolver = new CongestionSolver(congestionProfile, 8);
        dsm1 = new DSMPartner(0, 48, 8, 100, -1);
        dsm2 = new DSMPartner(0, 48, 8, 50, -1);
        register();
        sim.register(congestionSolver);
        sim.start();
        assertEquals(0, congestionSolver.getTotalRemediedCongestion(), 0);

    }

    private void register() {
        congestionSolver.registerDSMPartner(dsm1);
        congestionSolver.registerDSMPartner(dsm2);
    }

    @Test
    public void testPosActivation() {
        congestionSolver = new CongestionSolver(congestionProfile, 8);
        dsm1 = new DSMPartner(40, 48, 8, 100, -1);
        dsm2 = new DSMPartner(40, 48, 8, 50, -1);
        register();
        sim.register(congestionSolver);
        sim.start();
        assertNotEquals(0.0, congestionSolver.getTotalRemediedCongestion());
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm1.getCurrentActivations(), 0);
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm2.getCurrentActivations(), 0);
    }

    @Test
    public void testPosActivationNumberActivations() {
        int power = 100;
        congestionSolver = new CongestionSolver(congestionProfile, 8);
        dsm1 = new DSMPartner(40, 48, 8, power, -1);
        register();
        sim.register(congestionSolver);
        sim.start();
        assertNotEquals(0.0, congestionSolver.getTotalRemediedCongestion());
        assertEquals(DSMPartner.R3DPMAX_ACTIVATIONS,
                dsm1.getCurrentActivations(), 0);
        assertTrue(congestionSolver.getTotalRemediedCongestion() <= power
                * DSMPartner.R3DPMAX_ACTIVATIONS * 2);
    }

}
