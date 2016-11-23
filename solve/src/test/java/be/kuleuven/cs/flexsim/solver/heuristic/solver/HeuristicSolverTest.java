package be.kuleuven.cs.flexsim.solver.heuristic.solver;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class HeuristicSolverTest {
    private HeuristicSolver solver;
    private FlexAllocProblemContext context;
    private FlexibilityProvider first;
    private FlexibilityProvider second;
    private CongestionProfile profile;

    @Before
    public void setUp() throws IOException {
        this.profile = CongestionProfile.createFromCSV("smalltest.csv", "test");
        first = new FlexProvider(10,
                HourlyFlexConstraints.builder().maximumActivations(2).interActivationTime(3)
                        .activationDuration(2).build());
        second = new FlexProvider(25,
                HourlyFlexConstraints.builder().maximumActivations(2).interActivationTime(3)
                        .activationDuration(2).build());
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
        this.solver = new HeuristicSolver(context);
    }

    @Test
    public void solve() throws Exception {
        solver.solve();
        AllocResults solution = solver.getSolution();
        //        DSOOptimalSolver dsoOptimalSolver = new DSOOptimalSolver(context,
        //                AbstractOptimalSolver.Solver.GUROBI);
        //        dsoOptimalSolver.solve();
        //        System.out.println("Grb solution: " + dsoOptimalSolver.getSolution()
        // .getObjective());

    }

    @Test
    public void getSolution() throws Exception {

    }

}