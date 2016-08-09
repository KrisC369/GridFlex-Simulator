package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DSOOptimalSolverTest {
    private CongestionProfile profile;
    private DSOOptimalSolver solver;
    private FlexProvider provider1;
    private FlexProvider provider2;
    private static String column = "test";
    private static String file = "test.csv";

    @Before
    public void setUp() throws Exception {
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV(file, column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        solver = new DSOOptimalSolver(profile, 8);
        provider1 = new FlexProvider(200);
        provider2 = new FlexProvider(500);
    }

    @Test
    public void testInit() {
        initialize();
    }

    private void initialize() {
        solver.registerFlexProvider(provider1);
        solver.registerFlexProvider(provider2);
        assertEquals(2, solver.getProviders().size(), 2);
    }

    @Test
    public void testSolve() {
        initialize();
        System.out.println(solver.getProblem());
        solver.tick(1);
        AllocResults res = solver.getResults();
        for (FlexProvider p : solver.getProviders()) {
            int countActivation = 0;
            int countInter = 0;
            for (Boolean b : res.getAllocationResults().get(p)) {
                if (b) {
                    countActivation++;
                    if (countInter < p.getActivationConstraints().getInterActivationTime()) {
                        fail("not respecting IA." + countActivation + " " + countInter);
                    }
                    countInter = 0;
                } else {
                    countActivation = 0;
                    countInter++;
                }
                if (countActivation > p.getActivationConstraints().getActivationDuration()) {
                    fail("not accounting AD." + countActivation + " " + countInter);
                }

            }
        }
        System.out.println(res);
    }

}