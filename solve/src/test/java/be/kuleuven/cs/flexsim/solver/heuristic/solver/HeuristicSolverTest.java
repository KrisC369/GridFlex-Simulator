package be.kuleuven.cs.flexsim.solver.heuristic.solver;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexConstraints;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import be.kuleuven.cs.flexsim.solver.optimal.ConstraintConversion;
import be.kuleuven.cs.flexsim.solver.optimal.mip.MIPOptimalSolver;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class HeuristicSolverTest {
    private HeuristicSolver solver;
    private FlexAllocProblemContext context;
    private FlexibilityProvider first;
    private FlexibilityProvider second;
    private CongestionProfile profile;
    private AbstractOptimalSolver altSolver;
    private Logger logger = org.slf4j.LoggerFactory.getLogger(HeuristicSolverTest.class);

    @Before
    public void setUp() throws IOException {
        this.profile = CongestionProfile.createFromCSV("smalltest.csv", "test");
        first = new FlexProvider(400,
                HourlyFlexConstraints.builder().maximumActivations(2).interActivationTime(1)
                        .activationDuration(0.5).build());
        second = new FlexProvider(560,
                HourlyFlexConstraints.builder().maximumActivations(2).interActivationTime(1)
                        .activationDuration(0.5).build());
        initSolvers();
    }

    private void initSolvers() {
        this.context = new FlexAllocProblemContext() {

            @Override
            public Iterable<FlexibilityProvider> getProviders() {
                return Lists.newArrayList(first, second);
            }

            @Override
            public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                return profile;
            }
        };
        this.solver = new HeuristicSolver(context, true);
        this.altSolver = new MIPOptimalSolver(context, AbstractOptimalSolver.Solver.CPLEX);
    }

    @Test
    public void testSolve() throws Exception {
        solver.solve();
        AllocResults solution = solver.getSolution();
        testConstraints(solution);
    }

    @Test
    public void testMediumSolve() throws Exception {
        this.profile = CongestionProfile.createFromCSV("test.csv", "test");
        first = new FlexProvider(400,
                HourlyFlexConstraints.builder().maximumActivations(6).interActivationTime(2)
                        .activationDuration(2).build());
        second = new FlexProvider(560,
                HourlyFlexConstraints.builder().maximumActivations(6).interActivationTime(2)
                        .activationDuration(0.5).build());
        solver.solve();
        AllocResults solution = solver.getSolution();
        logger.info(solution.toString());
        testConstraints(solution);
    }

    @Test
    public void testLargeSolve() throws Exception {
        this.profile = CongestionProfile.createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
        first = new FlexProvider(4000,
                HourlyFlexConstraints.R3DP);
        second = new FlexProvider(2560,
                HourlyFlexConstraints.R3DP);

        solver.solve();
        AllocResults solution = solver.getSolution();
        testConstraints(solution);
    }

    @Test
    public void testMultiModels() throws Exception {
        this.profile = CongestionProfile.createFromCSV("test.csv", "test");
        this.context = new FlexAllocProblemContext() {

            @Override
            public Iterable<FlexibilityProvider> getProviders() {
                return Lists.newArrayList(first, second);
            }

            @Override
            public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                return profile;
            }
        };
        this.solver = new HeuristicSolver(context, true);
        HeuristicSolver newSolver = new HeuristicSolver(context, false);

        solver.solve();
        AllocResults solution1 = solver.getSolution();
        newSolver.solve();
        AllocResults solution2 = newSolver.getSolution();
        testConstraints(solution1);
        testConstraints(solution2);
        assertResultEquality(solution1, solution2);
    }

    private void assertResultEquality(AllocResults solution1, AllocResults solution2) {
        assertEquals(solution1.getObjective(), solution2.getObjective(), 0.1);
        assertEquals(solution1.getAllocationResults(), solution2.getAllocationResults());
    }

    @Test
    @Ignore
    public void testCompareResults() {
        solver.solve();
        AllocResults solution1 = solver.getSolution();
        logger.info(solution1.toString());
        altSolver.solve();
        AllocResults solution2 = altSolver.getSolution();
        logger.info(solution2.toString());
        assertResultEquality(solution1, solution2);
    }

    @Test
    @Ignore
    public void testCompareMediumResults() throws Exception {
        this.profile = CongestionProfile.createFromCSV("test.csv", "test");
        first = new FlexProvider(400,
                HourlyFlexConstraints.builder().maximumActivations(6).interActivationTime(2)
                        .activationDuration(0.5).build());
        second = new FlexProvider(560,
                HourlyFlexConstraints.builder().maximumActivations(6).interActivationTime(2)
                        .activationDuration(0.5).build());
        initSolvers();
        solver.solve();
        AllocResults solution1 = solver.getSolution();
        logger.info(solution1.toString());
        altSolver.solve();
        AllocResults solution2 = altSolver.getSolution();
        logger.info(solution2.toString());
        assertEquals(solution1.getObjective(), solution2.getObjective(), 0.1);
        //        assertEquals(solution1.getAllocationResults(), solution2.getAllocationResults());
    }

    @Test
    @Ignore
    public void testLargeCompareResults() throws Exception {
        this.profile = CongestionProfile.createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
        first = new FlexProvider(4000,
                HourlyFlexConstraints.R3DP);
        second = new FlexProvider(2560,
                HourlyFlexConstraints.R3DP);
        initSolvers();
        solver.solve();
        AllocResults solution1 = solver.getSolution();
        logger.info(solution1.toString());
        altSolver.setVerboseSolving(true);
        altSolver.solve();
        AllocResults solution2 = altSolver.getSolution();
        logger.info(solution2.toString());
        assertResultEquality(solution1, solution2);
    }

    private void testConstraints(AllocResults res) {
        if (res.equals(AllocResults.INFEASIBLE)) {
            fail("Results gotten are infeasible.");
        }
        testActivationDuration(res);
        testInterActivationTime(res);
        testHasActivations(res);
    }

    private void testHasActivations(AllocResults res) {
        for (FlexibilityProvider p : context.getProviders()) {
            FlexConstraints adapted = ConstraintConversion.fromHourlyToQuarterHourly(
                    p.getFlexibilityActivationConstraints());
            int countActivation = 0;
            int activationsSoFar = 0;
            for (Boolean b : res.getAllocationResults().get(p)) {
                if (b) {
                    countActivation++;
                } else {
                    if (countActivation > 0) {
                        activationsSoFar++;
                    }
                    countActivation = 0;
                }
            }
            if (countActivation != 0) {
                activationsSoFar++;
            }
            if (activationsSoFar == 0) {
                fail("No activations registered for provider: " + p);
            }
        }
    }

    private void testInterActivationTime(AllocResults res) {
        for (FlexibilityProvider p : context.getProviders()) {
            FlexConstraints adapted = ConstraintConversion.fromHourlyToQuarterHourly(
                    p.getFlexibilityActivationConstraints());
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
        for (FlexibilityProvider p : context.getProviders()) {
            FlexConstraints adapted = ConstraintConversion.fromHourlyToQuarterHourly(
                    p.getFlexibilityActivationConstraints());
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