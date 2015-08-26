package be.kuleuven.cs.flexsim.protocol.contractnet;

import be.kuleuven.cs.flexsim.protocol.AnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.Proposal;
import be.kuleuven.cs.flexsim.protocol.Responder;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class CNPResponder implements Responder<Proposal> {

    @Override
    public void callForProposal(AnswerAnticipator<Proposal> responder, Proposal arg) {

    }

}
