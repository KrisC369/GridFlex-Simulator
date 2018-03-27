package be.kuleuven.cs.gridflex.solvers.heuristic.domain;

import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.solvers.common.QHFlexibilityProvider;
import com.google.common.collect.Lists;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static org.apache.commons.math3.util.FastMath.min;

/**
 * Allocation instance of flex to profile.
 * To be used by optaplanner.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@PlanningSolution
public class Allocation implements Solution<HardSoftScore> {
    private static int SIZE = 35040;
    private List<QHFlexibilityProvider> providers;
    private List<ActivationAssignment> assignments;
    private List<Integer> startValueRange;
    private HardSoftScore score;
    private CongestionProfile profile;

    /**
     * Default allocation constructor.
     */
    public Allocation() {
    }

    /**
     * Optaplanner accessor.
     *
     * @param p The profile.
     */
    public void setProfile(CongestionProfile p) {
        this.profile = p;
    }

    /**
     * @return The list of assignments.
     */
    @PlanningEntityCollectionProperty
    public List<ActivationAssignment> getAllocationList() {
        return assignments;
    }

    /**
     * @return the list of providers.
     */
    public List<QHFlexibilityProvider> getProviders() {
        return providers;
    }

    /**
     * Set the list of providers.
     *
     * @param providers The providers.
     */
    public void setProviders(
            List<QHFlexibilityProvider> providers) {
        this.providers = providers;
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

    /**
     * @return The possible activation start values.
     */
    @ValueRangeProvider(id = "startPeriodRange")
    public List<Integer> getActivationStartValues() {

        return startValueRange;
    }

    /**
     * @param startValues The start values to be set by optaplanner.
     */
    public void setActivationStartValues(List<Integer> startValues) {
        this.startValueRange = startValues;
    }

    /**
     * @return The resolved congestion by this activation.
     */
    public double getResolvedCongestion() {
        int[][] acts = getAllocationMaps();

        return IntStream.range(0, profile.length()).filter(i -> activated(acts, i))
                .mapToDouble(i -> min(profile.value(i), getSum(acts, i))).sum();
    }

    /**
     * @return The binary data of 1's and 0's that represent the activations for all providers as
     * a 2dim array.
     */
    public int[][] getAllocationMaps() {
        int[][] acts = new int[providers.size()][profile.length()];
        for (int[] row : acts) {
            Arrays.fill(row, 0, profile.length(), 0);
        }
        for (int i = 0; i < providers.size(); i++) {
            QHFlexibilityProvider p = providers.get(i);
            for (ActivationAssignment aa : getAssignments()) {
                if (aa.isBound() && aa.getProvider().equals(p)) {
                    Arrays.fill(acts[i], aa.getStartIndex(),
                            (int) min(aa.getStartIndex() + aa.getProvider()
                                    .getQHFlexibilityActivationConstraints()
                                    .getActivationDuration(), profile.length()),
                            1);
                }
            }
        }
        return acts;
    }

    private boolean activated(int[][] acts, int idx) {
        int prod = 1;
        for (int i = 0; i < acts.length; i++) {
            prod *= (1 - acts[i][idx]);
        }
        return prod == 1 ? false : true;
    }

    private double getSum(int[][] acts, int idx) {
        return IntStream.range(0, acts.length).mapToDouble(
                i -> providers.get(i).getFlexibilityActivationRate().getUp() * acts[i][idx]).sum();
    }

    /**
     * @return The assignments.
     */
    public Collection<ActivationAssignment> getAssignments() {
        return assignments;
    }

    /**
     * Setting the list of assignments.
     *
     * @param assignments The assignments
     */
    public void setAssignments(
            List<ActivationAssignment> assignments) {
        this.assignments = assignments;
    }

}
