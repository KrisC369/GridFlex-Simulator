package be.kuleuven.cs.flexsim.solver.heuristic.solver.benchmark.persistence;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.solver.heuristic.domain.Allocation;
import be.kuleuven.cs.flexsim.solver.heuristic.solver.HeuristicSolver;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class HeuristicSolverIOTest {

    private HeuristicSolver solver;
    private FlexAllocProblemContext context;
    private FlexibilityProvider first;
    private FlexibilityProvider second;
    private CongestionProfile profile;

    @Before
    public void setUp() throws IOException {
        this.profile = CongestionProfile.createFromCSV("smalltest.csv", "test");
        first = new FlexProvider(400,
                HourlyFlexConstraints.builder().maximumActivations(2).interActivationTime(1)
                        .activationDuration(0.5).build());
        second = new FlexProvider(560,
                HourlyFlexConstraints.builder().maximumActivations(2).interActivationTime(1)
                        .activationDuration(0.5).build());
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
    public void write() throws Exception {
        Allocation allocation = solver.new AllocationGenerator().createAllocation();

        //        Path path = Paths.get("/tmp/foo/model_a1_1.txt");
        //
        //        Files.createDirectories(path.getParent());
        File f = new File("/tmp/foo/model_a1_1.txt");
        System.out.println(f.getPath());
        //new HeuristicSolverIO().write(allocation, f);

    }

}