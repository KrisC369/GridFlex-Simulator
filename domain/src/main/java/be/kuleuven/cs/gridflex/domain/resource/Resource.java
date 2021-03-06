package be.kuleuven.cs.gridflex.domain.resource;

import be.kuleuven.cs.gridflex.domain.util.Bufferable;

/**
 * Interface class for resource representations in the system.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface Resource extends Bufferable {

    /**
     * Return the needed process time for this resource at this moment.
     * 
     * @return the needed process time
     */
    int getCurrentNeededProcessTime();

    /**
     * Returns the maximum needed process time. The highest value it could be at
     * this point.
     * 
     * @return the maximum process time.
     */
    int getMaxNeededProcessTime();

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
