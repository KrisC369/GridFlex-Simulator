package be.kuleuven.cs.flexsim.domain.util.listener;

/**
 * Listener that does nothing.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class NoopListener implements Listener<Object> {
    private NoopListener() {
    }

    /**
     * The singleton for this noop listener.
     */
    public static final NoopListener INSTANCE = new NoopListener();

    public void eventOccurred(Object arg) {
    }
}
