package be.kuleuven.cs.gridflex.domain.resource;

/**
 * This Class SimpleResource implements the IResource interface and represents a
 * very simple resource concept that needs a specific amount of time to be
 * processed.
 */
public class SimpleResource implements Resource {

    private static final long serialVersionUID = 8674085582036232815L;

    /** The needed time. */
    private int neededTime;
    private int maxTime;

    /**
     * Instantiates a new simple resource.
     * 
     * @param needed
     *            the time needed to proces this resource.
     */
    protected SimpleResource(final int needed) {
        this.neededTime = needed;
        this.maxTime = needed;
    }

    /*
     * (non-Javadoc)
     * @see domain.IResource#getNeededProcessTime()
     */
    @Override
    public final int getCurrentNeededProcessTime() {
        return this.neededTime;
    }

    @Override
    public final boolean needsMoreProcessing() {
        return getCurrentNeededProcessTime() > 0;
    }

    @Override
    public void notifyOfHasBeenBuffered() {
    }

    /*
     * (non-Javadoc)
     * @see domain.IResource#process(int)
     */
    @Override
    public final void process(final int time) {
        this.neededTime -= time;
    }

    /**
     * Sets the neededTime to a new value.
     * 
     * @param newTime
     *            The new value for the neededprocessingTime.
     */
    protected final void setNeededTime(final int newTime) {
        this.neededTime = newTime;
        this.maxTime = newTime;
    }

    @Override
    public int getMaxNeededProcessTime() {
        return this.maxTime;
    }
}
