package be.kuleuven.cs.flexsim.domain.energy.dso.online.contractnet;

import be.kuleuven.cs.flexsim.protocol.Proposal;
import com.google.auto.value.AutoValue;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Optional;

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
        return getBeginMark().isPresent() || getEndMark().isPresent();
    }

    /**
     * Factory method
     *
     * @param target    the target imbalance volume that needs to be corrected.
     * @param valuation the valuation value associated with this proposal.
     * @return a nomination value object. //
     */
    static DSMProposal create(final String description, final double target,
            final double valuation, @Nullable final Integer begin, @Nullable final Integer end) {
        return new AutoValue_DSMProposal(description, target,
                Optional.ofNullable(begin), Optional.ofNullable(end),
                valuation);
    }
}
