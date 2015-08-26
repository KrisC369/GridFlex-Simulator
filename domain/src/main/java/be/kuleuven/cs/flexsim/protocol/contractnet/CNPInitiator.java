package be.kuleuven.cs.flexsim.protocol.contractnet;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import autovalue.shaded.com.google.common.common.collect.Maps;
import be.kuleuven.cs.flexsim.protocol.AnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.Initiator;
import be.kuleuven.cs.flexsim.protocol.Proposal;
import be.kuleuven.cs.flexsim.protocol.Responder;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public abstract class CNPInitiator implements Initiator<Proposal> {
    private List<Responder<Proposal>> responders;
    private Map<Proposal, AnswerAnticipator<Proposal>> props = Maps.newLinkedHashMap();
    private Proposal description;
    private int messageCount = 0;

    public CNPInitiator() {
        responders = Lists.newArrayList();
        this.messageCount = 0;
        this.props = Maps.newLinkedHashMap();
        this.description = new CNPProposal();
    }

    @Override
    public void registerResponder(Responder<Proposal> r) {
        this.responders.add(r);
    }

    public void sollicitWork(Proposal p) {
        resetCommunication();
        startCNP(p);
    }

    private void resetCommunication() {
        this.messageCount = 0;
        props = Maps.newLinkedHashMap();
    }

    private void startCNP(Proposal p) {
        this.description = new CNPProposal();
        final Map<Proposal, AnswerAnticipator<Proposal>> props = Maps.newLinkedHashMap();
        for (Responder<Proposal> r : responders) {
            r.callForProposal(new AnswerAnticipator<Proposal>() {

                @Override
                public void reject() {
                    phase1Reject(); // refuse
                }

                @Override
                public void affirmative(Proposal prop, AnswerAnticipator<Proposal> ant) {
                    phase1Accept(prop, ant); // propose
                }

            }, this.description);
        }
    }

    private void phase1Reject() {
        this.messageCount++;
    }

    private void phase1Accept(Proposal prop, AnswerAnticipator<Proposal> ant) {
        this.messageCount++;
        props.put(prop, ant);
        if (messageCount == responders.size()) {
            asynchronousPhase2();
        }
    }

    private void asynchronousPhase2() {
        if (!props.isEmpty()) {
            cnpPhaseTwo(props, description);
        } else {
            signalNoSolutionFound();
        }
    }

    protected abstract void signalNoSolutionFound();

    private void cnpPhaseTwo(Map<Proposal, AnswerAnticipator<Proposal>> props, Proposal description) {
        Proposal best = findBestProposal(Lists.newArrayList(props.keySet()), description);
        Map<Proposal, AnswerAnticipator<Proposal>> rejects = Maps.newLinkedHashMap(props);
        rejects.remove(best);
        notifyRejects(rejects);
        notifyAcceptPhase2(best, props);
    }

    private void notifyAcceptPhase2(Proposal best, Map<Proposal, AnswerAnticipator<Proposal>> props) {
        final Proposal description2 = description;
        props.get(best).affirmative(description2, new AnswerAnticipator<Proposal>() { // accept-proposal
            // Completion or failure notification.
            @Override
            public void affirmative(Proposal prop, AnswerAnticipator<Proposal> ant) { // inform-done
                // TODO Auto-generated method stub
            }

            @Override
            public void reject() { // failure
                // TODO Auto-generated method stub
            }
        });
    }

    private void notifyRejects(Map<Proposal, AnswerAnticipator<Proposal>> rejects) {
        for (AnswerAnticipator<Proposal> e : rejects.values()) {
            e.reject(); // reject-proposal
        }
    }

    /**
     * Find the best propsal in a list of proposals that fits the description of
     * a work unit given.
     * 
     * @param props
     * @param description
     * @return
     */
    public abstract Proposal findBestProposal(List<Proposal> props, Proposal description);

    /**
     * 
     * @return a copy of the responders list for this initiator.
     */
    public List<Responder<Proposal>> getResponders() {
        return Lists.newArrayList(responders);
    }
}
