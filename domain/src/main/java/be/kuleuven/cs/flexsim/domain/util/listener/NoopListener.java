package be.kuleuven.cs.flexsim.domain.util.listener;

/**
 * Listener that does nothing.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class NoopListener implements Listener<Object> {

    /**
     * The singleton for this noop listener.
     */
    public static final NoopListener INSTANCE = new NoopListener();

    private NoopListener() {
    }

    @Override
    public void eventOccurred(final Object arg) {
    }
}
