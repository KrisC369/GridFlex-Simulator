package be.kuleuven.cs.flexsim.solver.heuristic.solver;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.solver.heuristic.domain.ActivationAssignment;
import be.kuleuven.cs.flexsim.solver.heuristic.domain.Allocation;
import be.kuleuven.cs.flexsim.solver.heuristic.domain.OptaFlexProvider;
import be.kuleuven.cs.flexsim.solver.heuristic.domain.QHFlexibilityProvider;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.optaplanner.core.api.solver.SolverFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Heuristic solver making use of optaplanner to find a solution to flex allocation problems.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class HeuristicSolver implements Solver<AllocResults> {

    private FlexAllocProblemContext context;
    private Allocation solvedAlloc;

    HeuristicSolver(FlexAllocProblemContext context) {
        this.context = context;
    }

    @Override
    public void solve() {
        String filename = "HeuristicSolverConfig.xml";
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(classLoader.getResource(filename).getFile());
        SolverFactory<Allocation> solverFactory = SolverFactory.createFromXmlFile(file);
        org.optaplanner.core.api.solver.Solver<Allocation> solver = solverFactory.buildSolver();
        Allocation unsolvedAlloc = new AllocationGenerator().createAllocation();

        // Solve the problem
        solver.solve(unsolvedAlloc);
        this.solvedAlloc = (Allocation) solver.getBestSolution();

    }

    public void displayResult() {
        System.out.println("\nSolved with value:" + toDisplayString(solvedAlloc));
    }

    @Override
    public AllocResults getSolution() {
        List<QHFlexibilityProvider> providers = solvedAlloc.getProviders();
        ListMultimap<FlexibilityProvider, Boolean> actMap = ArrayListMultimap
                .create();
        int[][] allocationMaps = solvedAlloc.getAllocationMaps();
        for (int i = 0; i < allocationMaps.length; i++) {
            List<Boolean> toAdd = Lists.newArrayList();
            for (int j = 0; j < allocationMaps[i].length; j++) {
                toAdd.add(allocationMaps[i][j] == 1 ? true : false);
            }
            actMap.putAll(providers.get(i).getWrappedProvider(), toAdd);
        }
        if (solvedAlloc.getScore().isFeasible()) {
            return AllocResults.create(actMap, solvedAlloc.getResolvedCongestion());
        } else {
            return AllocResults.INFEASIBLE;
        }
    }

    private class AllocationGenerator {
        Allocation createAllocation() {
            List<QHFlexibilityProvider> providers = Lists.newArrayList();
            context.getProviders().forEach(p -> providers.add(new OptaFlexProvider(p)));
            List<ActivationAssignment> assignments = Lists.newArrayList();
            CongestionProfile profile = CongestionProfile
                    .createFromTimeSeries(context.getEnergyProfileToMinimizeWithFlex());
            int id = 0;
            for (QHFlexibilityProvider prov : providers) {
                for (int i = 0;
                     i < prov.getQHFlexibilityActivationConstraints()
                             .getMaximumActivations(); i++) {
                    ActivationAssignment actAss = ActivationAssignment
                            .create(id++, prov, profile);
                    assignments.add(actAss);
                }
            }
            Allocation a = new Allocation();
            a.setAssignments(assignments);
            a.setProfile(profile);
            a.setProviders(providers);
            a.setActivationStartValues(IntStream.range(0, profile.length()).boxed()
                    .collect(Collectors.toList()));
            return a;
        }
    }

    /**
     * Display the allocation results' to string.
     *
     * @param allocation The allocation to print
     * @return A string containing the resolved congestion for this allocation.
     */
    public static String toDisplayString(Allocation allocation) {
        double sum = allocation.getResolvedCongestion();
        StringBuilder displayString = new StringBuilder();
        displayString.append("Solved Congestion: ").append(sum);
        return displayString.toString();
    }
}