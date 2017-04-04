package be.kuleuven.cs.gridflex.protocol.contractnet;

import be.kuleuven.cs.gridflex.protocol.AnswerAnticipator;
import be.kuleuven.cs.gridflex.protocol.Initiator;
import be.kuleuven.cs.gridflex.protocol.Proposal;
import be.kuleuven.cs.gridflex.protocol.Responder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * @param <T> The concrete type of the proposal for the responders.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class ContractNetInitiator<T extends Proposal> implements Initiator<T> {

    private final List<Responder<T>> responders;
    private Map<T, AnswerAnticipator<T>> props;
    private Optional<T> description;
    private int messageCount;

    protected ContractNetInitiator() {
        responders = Lists.newArrayList();
        this.messageCount = 0;
        this.props = Maps.newLinkedHashMap();
        this.description = empty();
    }

    @Override
    public void registerResponder(final Responder<T> r) {
        this.responders.add(r);
    }

    /**
     * Signals this initiator that works need to be done. This method
     * immediately calls {@code ContractNetInitiator.getWorkUnitDescription()}.
     */
    public void sollicitWork() {
        final Optional<T> p = getWorkUnitDescription();
        resetCommunication();
        if (p.isPresent()) {
            startCNP(p.get());
        }
    }

    private void resetCommunication() {
        this.messageCount = 0;
        props = Maps.newLinkedHashMap();
    }

    private void startCNP(final T p) {
        this.description = ofNullable(p);
        for (final Responder<T> r : responders) {
            r.callForProposal(new AnswerAnticipator<T>() {

                @Override
                public void reject() {
                    phase1Reject(); // refuse
                }

                @Override
                public void affirmative(final T prop, final AnswerAnticipator<T> ant) {
                    phase1Accept(prop, ant); // propose
                }

            }, this.description.get());
        }
    }

    private void phase1Reject() {
        this.messageCount++;
        moveToPhase2();
    }

    private void phase1Accept(final T prop, final AnswerAnticipator<T> ant) {
        this.messageCount++;
        props.put(prop, ant);
        moveToPhase2();
    }

    protected void moveToPhase2() {
        if (messageCount == responders.size()) {
            asynchronousPhase2();
        }
    }

    private void asynchronousPhase2() {
        if (!props.isEmpty()) {
            cnpPhaseTwo(props, this.description.get());
        } else {
            signalNoSolutionFound();
        }
    }

    protected abstract void signalNoSolutionFound();

    private void cnpPhaseTwo(final Map<T, AnswerAnticipator<T>> props,
            final T description) {
        final Optional<T> best = findBestProposal(Lists.newArrayList(props.keySet()),
                description);
        final Map<T, AnswerAnticipator<T>> rejects = Maps.newLinkedHashMap(props);
        if (best.isPresent()) {
            rejects.remove(best.get());
            notifyRejects(rejects);
            notifyAcceptPhase2(best.get(), props);
        } else {
            signalNoSolutionFound();
        }
    }

    /**
     * Optional step to update the work description before being sent out to the
     * responders.
     *
     * @param best The winner of the auction.
     * @return An updated proposal.
     */
    public abstract T updateWorkDescription(T best);

    private void notifyAcceptPhase2(final T best,
            final Map<T, AnswerAnticipator<T>> props) {
        checkNotNull(props.get(best)).affirmative(updateWorkDescription(best),
                new AnswerAnticipator<T>() { // accept-proposal
                    // Completion or failure notification.
                    @Override
                    public void affirmative(final T prop,
                            final AnswerAnticipator<T> ant) { // inform-done
                        notifyWorkDone(prop);
                    }

                    @Override
                    public void reject() { // failure
                    }
                });
    }

    private void notifyRejects(final Map<T, AnswerAnticipator<T>> rejects) {
        // reject-proposal
        rejects.values().forEach(AnswerAnticipator::reject);
    }

    /**
     * Find the best propsal in a list of proposals that fits the description of
     * a work unit given.
     *
     * @param props       The proposals
     * @param description The original call.
     * @return the best fitting proposal.
     */

    public abstract Optional<T> findBestProposal(List<T> props, T description);

    /**
     * This method is called immediately after a sollicitWork-call and should
     * return a description of the work that needs to be done, including
     * relevant data, or it should return an empty optional if there is no work
     * to be done.
     *
     * @return an optional proposal to be used as a Call for Proposals or
     * nothing if no work is to be done.
     */
    public abstract Optional<T> getWorkUnitDescription();

    /**
     * @return a copy of the responders list for this initiator.
     */
    public List<Responder<T>> getResponders() {
        return Lists.newArrayList(responders);
    }

    /**
     * Notifies that a work package has been completed.
     *
     * @param prop the work package description of the completed work.
     */
    public abstract void notifyWorkDone(T prop);

}
