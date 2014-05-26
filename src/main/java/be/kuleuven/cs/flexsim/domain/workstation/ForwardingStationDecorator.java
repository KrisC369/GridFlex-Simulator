package be.kuleuven.cs.flexsim.domain.workstation;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
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
    public double getLastStepConsumption() {
        return getDelegate().getLastStepConsumption();
    }

    @Override
    public int getProcessedItemsCount() {
        return getDelegate().getProcessedItemsCount();
    }

    @Override
    public double getTotalConsumption() {
        return getDelegate().getTotalConsumption();
    }

    @Override
    public boolean isIdle() {
        return getDelegate().isIdle();
    }

    @Override
    public void afterTick(int t) {
        getDelegate().afterTick(t);
    }

    @Override
    public void tick(int t) {
        getDelegate().tick(t);
    }

    /**
     * Returns the delegate instance for this forwarding decorator.
     * 
     * @return the delegated instance.
     */
    protected final Workstation getDelegate() {
        return this.w;
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        List<SimulationComponent> toret = new ArrayList<>();
        toret.addAll(getDelegate().getSimulationSubComponents());
        return toret;
    }

    @Override
    public void setSpeedVsEConsumptionRatio(int consumptionShift,
            int speedShift, boolean favorSpeed) {
        getDelegate().setSpeedVsEConsumptionRatio(consumptionShift, speedShift,
                favorSpeed);
    }

}
