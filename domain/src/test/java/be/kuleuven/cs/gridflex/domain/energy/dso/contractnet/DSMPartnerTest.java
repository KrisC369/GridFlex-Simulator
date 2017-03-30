package be.kuleuven.cs.gridflex.domain.energy.dso.contractnet;

import be.kuleuven.cs.gridflex.domain.util.data.profiles.AbstractTimeSeriesImplementation;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.simulation.Simulator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class DSMPartnerTest {

    private static String column = "test";
    private static String file = "test.csv";
    private static int testsize = 600 - 1;
    private final int duration = 8;
    private final int interAct = 20;
    private final int allowedAct = 10;
    @Mock
    AbstractTimeSeriesImplementation profile;
    private AbstractCongestionSolver congestionSolver;
    private CongestionProfile abstractTimeSeriesImplementation;
    private DSMPartner dsm1;
    private DSMPartner dsm2;
    private Simulator sim;

    @Before
    public void setUp() throws Exception {
        try {
            abstractTimeSeriesImplementation = CongestionProfile
                    .createFromCSV(file, column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        congestionSolver = new CooperativeCongestionSolver(abstractTimeSeriesImplementation,
                8);
        dsm1 = new DSMPartner(allowedAct, interAct, duration, 1000, 1);
        dsm2 = new DSMPartner(allowedAct, interAct, duration, 500, 1);
        // when(abstractTimeSeriesImplementation.value(anyInt())).thenReturn(175.0);
        // when(abstractTimeSeriesImplementation.values()).thenReturn(new double[4 * 24 *
        // 365]);

        sim = Simulator.createSimulator(testsize);
        register();
    }

    private void register() {
        congestionSolver.registerDSMPartner(dsm1);
        congestionSolver.registerDSMPartner(dsm2);
        sim.register(congestionSolver);
    }

    @Test
    public void testInterAct() {
        sim.start();
        testProvider(dsm1);
        testProvider(dsm2);
        assertTrue(dsm1.getCurrentActivations() > 0);
        assertTrue(dsm2.getCurrentActivations() > 0);
    }

    private void testProvider(DSMPartner dsm12) {
        int countAct = 0;
        int countInter = duration;
        for (int i = 0; i < testsize; i++) {
            if (dsm1.getCurtailment(i) > 0) {
                countAct++;
                if (countAct > duration) {
                    fail("Act duration longer than allowed");
                }
                if (0 < countInter && countInter < duration) {
                    System.out.println(i + " " + countInter);
                    fail("InterAct too short");
                }
                countInter = 0;
            } else {
                countInter++;
                countAct = 0;
            }
        }
    }

    @Test
    public void testToString() {
        assertTrue(dsm1.toString()
                .contains(String.valueOf(dsm1.getCurrentActivations())));
        assertTrue(dsm1.toString()
                .contains(String.valueOf(dsm1.getFlexPowerRate())));
    }
}
