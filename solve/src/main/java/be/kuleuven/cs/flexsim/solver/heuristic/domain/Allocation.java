package be.kuleuven.cs.flexsim.solver.heuristic.domain;

import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.apache.commons.math3.util.FastMath;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@PlanningSolution
public class Allocation implements Solution<HardSoftScore> {
    private static int SIZE = 35040;
    private List<QHFlexibilityProvider> providers;
    private List<ActivationAssignment> assignments;
    private ListMultimap<QHFlexibilityProvider, ActivationAssignment> assignmentMap;
    private final List<Integer> startValueRange;
    private HardSoftScore score;
    private CongestionProfile profile;

    public Allocation() {
        this.startValueRange = IntStream.range(0, SIZE).boxed()
                .collect(Collectors.toList());
    }

    public void setProfile(CongestionProfile p) {
        this.profile = p;
    }

    public void setProviders(
            List<QHFlexibilityProvider> providers) {
        this.providers = providers;
    }

    public void setAssignments(
            List<ActivationAssignment> assignments) {
        this.assignments = assignments;
    }

    public ListMultimap<QHFlexibilityProvider, ActivationAssignment> getAssignmentMap() {
        return assignmentMap;
    }

    public void setAssignmentMap(
            ListMultimap<QHFlexibilityProvider, ActivationAssignment> assignmentMap) {
        this.assignmentMap = assignmentMap;
    }

    @PlanningEntityCollectionProperty
    public List<ActivationAssignment> getAllocationList() {
        return assignments;
    }

    //    @ValueRangeProvider(id = "providerRange")
    public List<QHFlexibilityProvider> getProviders() {
        return providers;
    }

    @ValueRangeProvider(id = "startPeriodRange")
    public List<Integer> getActivationStartValues() {
        return startValueRange;
    }

    public Collection<ActivationAssignment> getAssignments() {
        return assignmentMap.values();
    }

    @Override
    public HardSoftScore getScore() {
        return score;
    }

    @Override
    public void setScore(HardSoftScore hardSoftScore) {
        this.score = hardSoftScore;
    }

    @Override
    public Collection<?> getProblemFacts() {
        return Lists.newArrayList(getActivationStartValues());
    }

    public double getResolvedCongestion() {
        int[][] acts = getAllocationMaps();

        return IntStream.range(0, profile.length()).filter(i -> activated(acts, i))
                .mapToDouble(i -> FastMath.min(profile.value(i), getSum(acts, i))).sum();
    }

    public int[][] getAllocationMaps() {
        int[][] acts = new int[providers.size()][profile.length()];
        for (int[] row : acts) {
            Arrays.fill(row, 0, profile.length(), 0);
        }
        for (int i = 0; i < providers.size(); i++) {
            QHFlexibilityProvider p = providers.get(i);
            for (ActivationAssignment aa : assignmentMap.get(p)) {
                if (aa.isBound()) {
                    Arrays.fill(acts[i], aa.getStartIndex(),
                            (int) (aa.getStartIndex() + aa.getProvider()
                                    .getQHFlexibilityActivationConstraints()
                                    .getActivationDuration()),
                            1);
                }
            }
        }
        return acts;
    }

    private double getSum(int[][] acts, int idx) {
        return IntStream.range(0, acts.length).mapToDouble(
                i -> providers.get(i).getFlexibilityActivationRate().getUp() * acts[i][idx]).sum();
    }

    private boolean activated(int[][] acts, int idx) {
        int prod = 1;
        for (int i = 0; i < acts.length; i++) {
            prod *= (1 - acts[i][idx]);
        }
        return prod == 1 ? false : true;
    }

}
