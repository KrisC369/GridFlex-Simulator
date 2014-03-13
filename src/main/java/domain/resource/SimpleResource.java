package domain.resource;

/**
 * This Class SimpleResource implements the IResource interface and represents a
 * very simple resource concept that needs a specific amount of time to be
 * processed.
 */
public class SimpleResource implements IResource {

    /** The needed time. */
    private int neededTime;

    /**
     * Instantiates a new simple resource.
     * 
     * @param needed
     *            the time needed to proces this resource.
     */
    protected SimpleResource(int needed) {
        this.neededTime = needed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.IResource#getNeededProcessTime()
     */
    @Override
    public int getCurrentNeededProcessTime() {
        return this.neededTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see domain.IResource#process(int)
     */
    @Override
    public void process(int time) {
        this.neededTime -= time;
    }

    @Override
    public boolean needsMoreProcessing() {
        return getCurrentNeededProcessTime() > 0;
    }

    @Override
    public void notifyOfHasBeenBuffered() {
    }

    /**
     * Sets the neededTime to a new value.
     * 
     * @param newTime
     */
    protected final void setNeededTime(int newTime) {
        this.neededTime = newTime;
    }
}
