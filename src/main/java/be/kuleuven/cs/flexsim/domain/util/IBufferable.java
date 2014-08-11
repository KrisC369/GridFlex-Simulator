package be.kuleuven.cs.flexsim.domain.util;

/**
 * Interface representing a bufferable instance.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface IBufferable {

    /**
     * Sends a notification to this bufferable instance that it has entered a
     * buffer.
     */
    void notifyOfHasBeenBuffered();
}
