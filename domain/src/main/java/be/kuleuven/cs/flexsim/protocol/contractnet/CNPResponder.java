package be.kuleuven.cs.flexsim.protocol.contractnet;

import be.kuleuven.cs.flexsim.protocol.AnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.NoOpAnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.Proposal;
import be.kuleuven.cs.flexsim.protocol.Responder;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 */
public abstract class CNPResponder<T extends Proposal> implements Responder<T> {

    @Override
    public void callForProposal(AnswerAnticipator<T> responder, T arg) {
        try {
            T ownProp = makeProposalForCNP(arg);
            responder.affirmative(ownProp, new AnswerAnticipator<T>() {

                @Override
                public void affirmative(T prop, AnswerAnticipator<T> ant) { // accept-proposal
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

    private void doWorkFor(T prop, AnswerAnticipator<T> ant) {
        boolean succeeded = performWorkUnitFor(prop);
        if (succeeded) {
            ant.affirmative(prop, new NoOpAnswerAnticipator<T>());
        } else {
            ant.reject();
        }
    }

    protected abstract T makeProposalForCNP(T arg)
            throws CanNotFindProposalException;

    protected abstract boolean performWorkUnitFor(T arg);

    /**
     * Exception signalling that a proposal cannot be constructed from the given
     * data.
     * 
     * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
     */
    public static class CanNotFindProposalException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = -7249264744599267326L;

    }

}
