package be.kuleuven.cs.flexsim.protocol;

/**
 * A generic no operation answer anticipator that does nothing.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The generic parameter T.
 */
public final class NoOpAnswerAnticipator<T> implements AnswerAnticipator<T> {

    /**
     * 
     */
    public NoOpAnswerAnticipator() {
    }

    @Override
    public void reject() {
    }

    @Override
    public void affirmative(T prop, AnswerAnticipator<T> ant) {
    }
}
