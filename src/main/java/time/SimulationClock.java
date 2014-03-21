package time;

/**
 * A basic clock representation for the simulation component of this software.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class SimulationClock implements Clock {

    /** The timecount of this clock. */
    private int timecount;

    /**
     * Public default constructor.
     */
    public SimulationClock() {
        this.timecount = 0;
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

    @Override
    public int getTimeCount() {
        return timecount;
    }

    /**
     * Reset this clock instance back to zero.
     */
    public void resetTime() {
        timecount = 0;

    }
}
