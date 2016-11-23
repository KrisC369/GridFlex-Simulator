package be.kuleuven.cs.flexsim.solver.heuristic.domain;

import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import org.apache.commons.math3.util.FastMath;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.stream.IntStream;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@PlanningEntity
public class ActivationAssignment {

    private static final double EPS = 0.001;
    //    @PlanningVariable(valueRangeProviderRefs = { "providerRange" })
    private QHFlexibilityProvider provider;
    @PlanningVariable(valueRangeProviderRefs = { "startPeriodRange" })
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

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public int getId() {
        return id;
    }

    public int getLastUnavailableIndex() {
        double idx = startIndex + provider.getQHFlexibilityActivationConstraints()
                .getActivationDuration()
                + provider.getQHFlexibilityActivationConstraints().getInterActivationTime();
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
                        FastMath.min(profile.length(),
                                startIndex + (int) provider.getQHFlexibilityActivationConstraints()
                                        .getActivationDuration())).mapToDouble(i -> FastMath
                        .min(profile.value(i),
                                provider.getFlexibilityActivationRate().getUp()))
                .sum();
    }

    public static ActivationAssignment create(int i, QHFlexibilityProvider p,
            CongestionProfile profile) {
        ActivationAssignment aa = new ActivationAssignment();
        aa.setId(i);
        aa.setProvider(p);

        aa.setProfile(profile);
        return aa;
    }

    boolean isBound() {
        return getStartIndex() != null;
    }
}
