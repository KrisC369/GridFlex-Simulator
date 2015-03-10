package be.kuleuven.cs.flexsim.domain.energy.tso.contractual;

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
