package domain;

/**
 * Interface class for resource representations in the system.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface IResource {
    /**
     * Return the needed process time for this resource.
     * 
     * @return
     */
    int getNeededProcessTime();

    /**
     * Indicate that 'time' numbers of process steps have been completed.
     * 
     * @param time
     *            the amount of completed time steps.
     */
    void process(int time);
}
