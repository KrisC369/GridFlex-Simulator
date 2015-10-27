package be.kuleuven.cs.flexsim.domain.energy.dso;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

import be.kuleuven.cs.flexsim.protocol.Proposal;

/**
 * Represents a proposal for DSM allocations.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@AutoValue
public abstract class DSMProposal implements Proposal {

    /**
     * @return A description of this proposal.
     */
    public abstract String getDescription();

    /**
     * @return The target value for this proposal.
     */
    public abstract double getTargetValue();

    /**
     * @return The demarcation for the start of the event in the proposal.
     */
    public abstract Optional<Integer> getBeginMark();

    /**
     * @return The demarcation for the start of the event in the proposal.
     */
    public abstract Optional<Integer> getEndMark();

    /**
     * @return The value for the agent supplying this proposal.
     */
    public abstract double getValuation();

    /**
     * @return true if the optionals for the time constraints are not absent.
     */
    public boolean hasTimeConstraints() {
        return getBeginMark().isPresent() && getEndMark().isPresent();
    }

    /**
     * Factory method
     *
     * @param target
     *            the target imbalance volume that needs to be corrected.
     * @param result
     *            the target imbalance volume that was effectively remedied.
     * @return a nomination value object. //
     */
    static DSMProposal create(String description, double target,
            double valuation, @Nullable Integer begin, @Nullable Integer end) {
        return new AutoValue_DSMProposal(description, target,
                Optional.fromNullable(begin), Optional.fromNullable(end),
                valuation);
    }
}