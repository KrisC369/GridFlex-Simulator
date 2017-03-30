package be.kuleuven.cs.gridflex.experimentation.tosg.benchmark.persistence;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.solvers.heuristic.domain.Allocation;
import be.kuleuven.cs.gridflex.solvers.heuristic.solver.HeuristicSolver;
import com.google.common.collect.Lists;
import org.eclipse.jdt.annotation.Nullable;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Test class for performing benchmarking.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class HeuristicSolverIO implements SolutionFileIO {
    private static Logger logger = LoggerFactory.getLogger(HeuristicSolverIO.class);
    @Nullable
    private FlexibilityProvider first;
    @Nullable
    private FlexibilityProvider second;
    @Nullable
    private CongestionProfile profile;

    HeuristicSolverIO() {
        first = null;
        second = null;
        profile = null;
    }

    @Override
    public String getInputFileExtension() {
        return ".csv";
    }

    @Override
    public String getOutputFileExtension() {
        return "";
    }

    @Override
    public Allocation read(File file) {
        HeuristicSolver solver;
        FlexAllocProblemContext context;
        double v1 = 1000;
        double v2 = 800;
        if (file.getPath().contains("model_a1_2")) {
            v1 = 300;
            v2 = 600;
        } else if (file.getPath().contains("model_a1_3")) {
            v1 = 1400;
            v2 = 2000;
        }
        try {
            this.profile = CongestionProfile.createFromCSV(
                    "be/kuleuven/cs/flexsim/experimentation/data/2kwartOpEnNeer.csv", "verlies aan "
                            + "energie");
            first = new FlexProvider(v1,
                    HourlyFlexConstraints.R3DP);
            second = new FlexProvider(v2,
                    HourlyFlexConstraints.R3DP);
            context = new FlexAllocProblemContext() {

                @Override
                public Collection<FlexibilityProvider> getProviders() {
                    return Lists.newArrayList(first, second);
                }

                @Override
                public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                    return profile;
                }
            };
            solver = HeuristicSolver.createFullSatHeuristicSolver(context);
        } catch (IOException e) {
            logger.error("IOException caught.", e);
            throw new RuntimeException(e);
        }
        return solver.new AllocationGenerator().createAllocation();
    }

    @Override
    public void write(Solution solution, File file) {
        throw new UnsupportedOperationException();
    }
}
