package time;

/**
 * A basic clock representation for the simulation component of this software.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class Clock {

    /** The timecount of this clock. */
    private int timecount;

    /**
     * Public default constructor.
     */
    public Clock() {
        this.timecount = 0;
    }

    /**
     * Get the time count for this clock.
     * 
     * @return The amount of times this clock has had a time step added.
     */
    public int getTimeCount() {
        return timecount;
    }

    /**
     * Add a number of time steps to this clock instance.
     * 
     * @param step
     *            the amount of time steps to add.
     */
    public void addTimeStep(int step) {
        this.timecount += step;
    }

    /**
     * Reset this clock instance back to zero.
     */
    public void resetTime() {
        timecount = 0;

    }
}
