package be.kuleuven.cs.flexsim.domain.util.listener;

/**
 * Listener that multiplexes other listeners.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <A>
 *            The type of the event arguments.
 *
 */
public final class MultiplexListener<A> implements Listener<A> {
    final Listener<? super A> l1;
    final Listener<? super A> l2;

    private MultiplexListener(Listener<? super A> l1, Listener<? super A> l2) {
        this.l1 = l1;
        this.l2 = l2;
    }

    @Override
    public void eventOccurred(A arg) {
        l1.eventOccurred(arg);
        l2.eventOccurred(arg);
    }

    /**
     * Adds listener l2 to the current tree of listeners.
     * 
     * @param l1
     *            The listener to add to.
     * @param l2
     *            The listener to add.
     * @return The new multiplexListener.
     */
    public static <A> Listener<? super A> plus(Listener<? super A> l1,
            Listener<? super A> l2) {
        if (l1 == NoopListener.INSTANCE) {
            return l2;
        }
        if (l2 == NoopListener.INSTANCE) {
            return l1;
        }
        return new MultiplexListener<>(l1, l2);
    }
}
