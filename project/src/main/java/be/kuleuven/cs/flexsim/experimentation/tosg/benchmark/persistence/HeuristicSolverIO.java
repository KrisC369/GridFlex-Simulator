package be.kuleuven.cs.flexsim.experimentation.tosg.benchmark.persistence;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.solver.heuristic.domain.Allocation;
import be.kuleuven.cs.flexsim.solver.heuristic.solver.HeuristicSolver;
import com.google.common.collect.Lists;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class HeuristicSolverIO implements SolutionFileIO {
    private static Logger logger = LoggerFactory.getLogger(HeuristicSolverIO.class);
    private HeuristicSolver solver;
    private FlexAllocProblemContext context;
    private FlexibilityProvider first;
    private FlexibilityProvider second;
    private CongestionProfile profile;

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
        Allocation readCase = null;
        try {
            this.profile = CongestionProfile.createFromCSV("2kwartOpEnNeer.csv", "verlies aan "
                    + "energie");
            first = new FlexProvider(4000,
                    HourlyFlexConstraints.R3DP);
            second = new FlexProvider(2560,
                    HourlyFlexConstraints.R3DP);
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return solver.new AllocationGenerator().createAllocation();
    }

    @Override
    public void write(Solution solution, File file) {
        throw new UnsupportedOperationException();
    }
}
