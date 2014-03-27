package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * Abstract base class implementing the decorator pattern for Workstation
 * instances.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class ForwardingStationDecorator implements Workstation {

    private final Workstation w;

    /**
     * Default constructor that has to be called by subclasses.
     * 
     * @param ws
     *            the delegate workstation of this decorator
     */
    ForwardingStationDecorator(Workstation ws) {
        this.w = ws;
    }

    @Override
    public void initialize(SimulationContext context) {
        getDelegate().initialize(context);
    }

    @Override
    public int getLastStepConsumption() {
        return getDelegate().getLastStepConsumption();
    }

    @Override
    public int getProcessedItemsCount() {
        return getDelegate().getProcessedItemsCount();
    }

    @Override
    public int getTotalConsumption() {
        return getDelegate().getTotalConsumption();
    }

    @Override
    public boolean isIdle() {
        return getDelegate().isIdle();
    }

    @Override
    public void afterTick() {
        getDelegate().afterTick();
    }

    @Override
    public void tick() {
        getDelegate().tick();
    }

    /**
     * Returns the delegate instance for this forwarding decorator.
     * 
     * @return the delegated instance.
     */
    protected final Workstation getDelegate() {
        return this.w;
    }

}
