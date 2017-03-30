package be.kuleuven.cs.gridflex.solvers.heuristic.domain.comparators;

import be.kuleuven.cs.gridflex.solvers.heuristic.domain.ActivationAssignment;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class ActivationAssignmentDifficultyComparator
        implements Comparator<ActivationAssignment>, Serializable {
    private static final long serialVersionUID = -4701403681060423663L;

    @Override
    public int compare(ActivationAssignment o1, ActivationAssignment o2) {
        return new CompareToBuilder()
                .append(o1.getProvider().getFlexibilityActivationRate().getUp(),
                        o2.getProvider().getFlexibilityActivationRate().getUp())
                .append(o1.getProvider().getQHFlexibilityActivationConstraints()
                                .getInterActivationTime(),
                        o2.getProvider().getQHFlexibilityActivationConstraints()
                                .getInterActivationTime()).append(o1.getId(), o2.getId())
                .toComparison();
    }
}
