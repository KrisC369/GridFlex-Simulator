package be.kuleuven.cs.flexsim.solvers.heuristic.domain;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.solvers.heuristic.solver.HeuristicSolver;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class ActivationAssignmentTest {
    private HeuristicSolver solver;
    private FlexAllocProblemContext context;
    private FlexibilityProvider first;
    private FlexibilityProvider second;
    private CongestionProfile profile;
    private ActivationAssignment aa1;
    private ActivationAssignment aa2;

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
        OptaFlexProvider prov1 = new OptaFlexProvider(first);
        OptaFlexProvider prov2 = new OptaFlexProvider(second);
        aa1 = ActivationAssignment.create(1, prov1, profile);
        aa2 = ActivationAssignment.create(1, prov2, profile);
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
        this.solver = HeuristicSolver.createFullSatHeuristicSolver(context);
    }

    @Test
    public void isOverlapping() throws Exception {
        aa1.setStartIndex(14);
        aa2.setStartIndex(14);
        assertTrue(aa1.isOverlapping(aa2));
        aa2.setStartIndex(16);
        assertFalse(aa1.isOverlapping(aa2));
    }

    @Test
    public void energyLostInOverlap() throws Exception {
        aa1.setStartIndex(14);
        aa2.setStartIndex(15);
        assertEquals(10, aa1.energyLostInOverlap(Lists.newArrayList(aa2)), 0);
    }

    @Test
    public void isActiveAt() throws Exception {
        aa1.setStartIndex(14);
        aa2.setStartIndex(14);
        assertTrue(ActivationAssignment.isActiveAt(aa1, 14));
        assertTrue(ActivationAssignment.isActiveAt(aa1, 15));
        assertFalse(ActivationAssignment.isActiveAt(aa1, 16));
    }

}