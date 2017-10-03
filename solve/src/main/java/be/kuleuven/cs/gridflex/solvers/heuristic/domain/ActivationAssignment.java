package be.kuleuven.cs.gridflex.solvers.heuristic.domain;

import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.solvers.common.QHFlexibilityProvider;
import be.kuleuven.cs.gridflex.solvers.heuristic.domain.comparators
        .ActivationAssignmentDifficultyComparator;
import be.kuleuven.cs.gridflex.solvers.heuristic.domain.comparators.IntegerStrengthComparator;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.List;
import java.util.stream.IntStream;

import static org.apache.commons.math3.util.FastMath.min;

/**
 * An assignment of activation of one providers' flexibility.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@PlanningEntity(difficultyComparatorClass = ActivationAssignmentDifficultyComparator.class)
public class ActivationAssignment {

    private static final double EPS = 0.001;
    private QHFlexibilityProvider provider;
    private Integer startIndex;
    private CongestionProfile profile;
    private int id;

    public QHFlexibilityProvider getProvider() {
        return provider;
    }

    public void setProvider(QHFlexibilityProvider provider) {
        this.provider = provider;
    }

    public void setId(int i) {
        this.id = i;
    }

    public void setProfile(CongestionProfile profile) {
        this.profile = profile;
    }

    public CongestionProfile getProfile() {
        return profile;
    }

    @PlanningVariable(valueRangeProviderRefs = {
            "startPeriodRange" }, strengthComparatorClass = IntegerStrengthComparator.class)
    public Integer getStartIndex() {
        return startIndex;
    }

    public Integer getEndIndex() {
        return min(startIndex + (int) (provider.getQHFlexibilityActivationConstraints()
                .getActivationDuration()), profile.length());
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public int getId() {
        return id;
    }

    /**
     * the last index of the unavailability period.
     *
     * @return
     */
    public int getLastUnavailableIndex() {
        double idx = startIndex + provider.getQHFlexibilityActivationConstraints()
                .getActivationDuration()
                + provider.getQHFlexibilityActivationConstraints().getInterActivationTime() - 1;
        if (idx % 1 > EPS) {
            throw new IllegalStateException("Casting to int will go wrong with: " + idx);
        }
        return (int) idx;
    }

    public double getResolvedCongestion() {
        if (startIndex == null) {
            return 0;
        }
        return IntStream
                .range(startIndex,
                        min(profile.length(),
                                startIndex + (int) provider.getQHFlexibilityActivationConstraints()
                                        .getActivationDuration())).mapToDouble(i ->
                        min(profile.value(i),
                                provider.getFlexibilityActivationRate().getUp()))
                .sum();
    }

    public boolean isOverlapping(ActivationAssignment other) {
        return (getStartIndex() < other.getEndIndex()) && (getEndIndex() > other.getStartIndex());
    }

    public double energyLostInOverlap(List<ActivationAssignment> acts) {
        double lost = 0;
        for (int i = getStartIndex(); i < getEndIndex(); i++) {
            double activated = getProvider().getFlexibilityActivationRate().getUp();
            //sum all activations with my activation
            for (ActivationAssignment a : acts) {
                if (isActiveAt(a, i)) {
                    activated += a.getProvider().getFlexibilityActivationRate().getUp();
                }
            }
            double diff = (profile.value(i) - activated);
            if (diff < 0) {
                lost -= diff;
            }
        }
        return lost;
    }

    public static boolean isActiveAt(ActivationAssignment aa, int i) {
        return (aa.getStartIndex() <= i) && (i < aa.getEndIndex());
    }

    public static ActivationAssignment create(int i, QHFlexibilityProvider p,
            CongestionProfile profile) {
        ActivationAssignment aa = new ActivationAssignment();
        aa.setId(i);
        aa.setProvider(p);
        aa.setProfile(profile);
        return aa;
    }

    public boolean isBound() {
        return getStartIndex() != null;
    }

    @Override
    public String toString() {
        return "ActivationAssignment{" +
                "id=" + id +
                ", startIndex=" + startIndex +
                ", provider=" + provider +
                '}';
    }
}
