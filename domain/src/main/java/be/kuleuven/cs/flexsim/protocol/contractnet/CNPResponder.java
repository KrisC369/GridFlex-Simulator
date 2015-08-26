package be.kuleuven.cs.flexsim.protocol.contractnet;

import be.kuleuven.cs.flexsim.protocol.AnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.NoOpAnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.Proposal;
import be.kuleuven.cs.flexsim.protocol.Responder;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public abstract class CNPResponder implements Responder<Proposal> {

    @Override
    public void callForProposal(AnswerAnticipator<Proposal> responder, Proposal arg) {
        try {
            Proposal ownProp = makeProposalForCNP(arg);
            responder.affirmative(ownProp, new AnswerAnticipator<Proposal>() {

                @Override
                public void affirmative(Proposal prop, AnswerAnticipator<Proposal> ant) { // accept-proposal
                    doWorkFor(prop, ant);
                }

                @Override
                public void reject() { // reject-proposal
                    // do nothing... out of luck...
                }
            });
        } catch (CanNotFindProposalException e) {
            responder.reject();
        }

    }

    private void doWorkFor(Proposal prop, AnswerAnticipator<Proposal> ant) {
        boolean succeeded = performWorkUnitFor(prop);
        if (succeeded) {
            ant.affirmative(prop, new NoOpAnswerAnticipator<Proposal>());
        } else {
            ant.reject();
        }
    }

    protected abstract Proposal makeProposalForCNP(Proposal arg) throws CanNotFindProposalException;

    protected abstract boolean performWorkUnitFor(Proposal arg);

    /**
     * Exception signalling that a proposal cannot be constructed from the given
     * data.
     * 
     * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
     *
     */
    public class CanNotFindProposalException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = -7249264744599267326L;

    }

}
