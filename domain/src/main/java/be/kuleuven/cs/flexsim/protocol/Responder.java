package be.kuleuven.cs.flexsim.protocol;

/**
 * @param <T> The type of the responder object.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
public interface Responder<T> {
    /**
     * @param responder
     * @param arg
     */
    void callForProposal(AnswerAnticipator<T> responder, T arg);

}
