package be.kuleuven.cs.flexsim.protocol;

/**
 * Represents a callback interface for 2 way confirmation- or rejection
 * dialogues.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The subject of the discussion.
 */
public interface AnswerAnticipator<T> {
    /**
     * Send an affirmative notification.
     * 
     * @param prop
     *            The proposal or subject of the discours.
     * @param ant
     *            The callback for further discussion.
     */
    void affirmative(T prop, AnswerAnticipator<T> ant);

    /**
     * Reject
     */
    void reject();

    /**
     * 
     */
    public static AnswerAnticipator<?> NOOP = new NoOpAnswerAnticipator<>();
}
