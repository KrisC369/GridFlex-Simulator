package domain.resource;

import domain.util.IBufferable;

/**
 * Interface class for resource representations in the system.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface IResource extends IBufferable {

    /**
     * Return the needed process time for this resource at this moment.
     * 
     * @return the needed process time
     */
    int getCurrentNeededProcessTime();

    /**
     * Returns whether this resource needs more processing at the current
     * station.
     * 
     * @return whether this resource needs more processing.
     */
    boolean needsMoreProcessing();

    /**
     * Indicate that 'time' numbers of process steps have been completed.
     * 
     * @param time
     *            the amount of completed time steps.
     */
    void process(int time);
}
