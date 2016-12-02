package be.kuleuven.cs.flexsim.protocol.contractnet;

import be.kuleuven.cs.flexsim.protocol.AnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.NoOpAnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.Proposal;
import be.kuleuven.cs.flexsim.protocol.Responder;
import org.slf4j.LoggerFactory;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The type of the proposal for the responders.
 */
public abstract class CNPResponder<T extends Proposal> implements Responder<T> {

    @Override
    public void callForProposal(final AnswerAnticipator<T> responder, final T arg) {
        try {
            final T ownProp = makeProposalForCNP(arg);
            responder.affirmative(ownProp, new AnswerAnticipator<T>() {

                @Override
                public void affirmative(final T prop, final AnswerAnticipator<T> ant) { // accept-proposal
                    doWorkFor(prop, ant);
                }

                @Override
                public void reject() { // reject-proposal
                    // do nothing... out of luck...
                }
            });
        } catch (final CanNotFindProposalException e) {
            LoggerFactory.getLogger(CNPResponder.class)
                    .debug("No proposal found for a certain agent.", e);
            responder.reject();
        }

    }

    private void doWorkFor(final T prop, final AnswerAnticipator<T> ant) {
        final boolean succeeded = performWorkUnitFor(prop);
        if (succeeded) {
            ant.affirmative(prop, new NoOpAnswerAnticipator<>());
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
