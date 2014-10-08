package be.kuleuven.cs.flexsim.domain.energy.tso;

import be.kuleuven.cs.flexsim.domain.util.listener.Listener;

/**
 * This interface represents an entity capable of signifying imbalances in the
 * grid.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface BalancingSignal {
    /**
     * Get the current value of the signal.
     * 
     * @param timeMark
     *            the time mark to specify when current is not clear enough.
     * @return the value.
     */
    int getCurrentImbalance();

    /**
     * Add a new listener for new steer value requests to this tso.
     * 
     * @param listener
     *            The listener to add.
     */
    void addNewBalanceValueListener(Listener<? super Integer> listener);
}
