package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.domain.energy.dso.offline.r3dp.FlexConstraints;
import be.kuleuven.cs.flexsim.domain.energy.dso.offline.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.offline.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import be.kuleuven.cs.flexsim.solver.optimal.ConstraintConversion;
import be.kuleuven.cs.flexsim.solver.optimal.dso.DSOOptimalSolver;
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
    private HourlyFlexConstraints constraints;
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
        constraints = HourlyFlexConstraints.builder().interActivationTime(5).interActivationTime(4)
                .maximumActivations(20).build();
        solver = new DSOOptimalSolver(profile, AbstractOptimalSolver.Solver.CPLEX);
        provider1 = new FlexProvider(200, constraints);
        provider2 = new FlexProvider(500, constraints);
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
        solver.tick(1);
        AllocResults res = solver.getResults();
        testConstraints(res);
    }

    @Test
    public void testReal() {
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
            solver = new DSOOptimalSolver(profile, AbstractOptimalSolver.Solver.CPLEX);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        initialize();
        solver.tick(1);
        AllocResults res = solver.getResults();
        testConstraints(res);
    }

    private void testConstraints(AllocResults res) {
        testActivationDuration(res);
        testInterActivationTime(res);
    }

    private void testInterActivationTime(AllocResults res) {
        for (FlexProvider p : solver.getProviders()) {
            FlexConstraints adapted = ConstraintConversion.fromHourlyToQuarterHourly(
                    p.getActivationConstraints());
            int countInter = 0;
            boolean wasActive = false;
            for (Boolean b : res.getAllocationResults().get(p)) {
                if (b) {
                    if (wasActive && countInter > 0 && countInter < adapted
                            .getInterActivationTime()) {
                        fail("not respecting IA." + countInter);
                    }
                    wasActive = true;
                    countInter = 0;
                } else {
                    countInter++;
                }
            }
        }
    }

    private void testActivationDuration(AllocResults res) {
        for (FlexProvider p : solver.getProviders()) {
            FlexConstraints adapted = ConstraintConversion.fromHourlyToQuarterHourly(
                    p.getActivationConstraints());
            int countActivation = 0;
            for (Boolean b : res.getAllocationResults().get(p)) {
                if (b) {
                    countActivation++;
                } else {
                    countActivation = 0;
                }
                if (countActivation > adapted.getActivationDuration()) {
                    fail("not accounting AD." + countActivation + " ");
                }
            }
        }
    }

}