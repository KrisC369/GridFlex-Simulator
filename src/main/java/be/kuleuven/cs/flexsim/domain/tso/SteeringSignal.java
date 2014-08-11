package be.kuleuven.cs.flexsim.domain.tso;

/**
 * This interface represents an entity capable of signifying imbalances in the
 * grid.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface SteeringSignal {
    /**
     * Get the current value of the signal.
     * 
     * @param timeMark
     *            the time mark to specify when current is not clear enough.
     * @return the value.
     */
    int getCurrentValue(int timeMark);
}
