package be.kuleuven.cs.gridflex.domain.workstation;

import be.kuleuven.cs.gridflex.simulation.SimulationComponent;
import be.kuleuven.cs.gridflex.simulation.SimulationContext;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Abstract base class implementing the decorator pattern for Workstation
 * instances.
 *
 * @param <T> The type of workstation we are decorating. Only Workstation
 *            interface methods are automatically delegated. Specific subtype
 *            methods should be delegated to in extending delegators.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class ForwardingStationDecorator<T extends Workstation>
        implements Workstation {

    private final T w;

    /**
     * Default constructor that has to be called by subclasses.
     *
     * @param ws the delegate workstation of this decorator
     */
    ForwardingStationDecorator(final T ws) {
        this.w = ws;
    }

    @Override
    public void initialize(final SimulationContext context) {
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
    public void afterTick(final int t) {
        getDelegate().afterTick(t);
    }

    @Override
    public void tick(final int t) {
        getDelegate().tick(t);
    }

    /**
     * Returns the delegate instance for this forwarding decorator.
     *
     * @return the delegated instance.
     */
    protected final T getDelegate() {
        return this.w;
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        return Lists.newArrayList(getDelegate().getSimulationSubComponents());
    }

    @Override
    public void acceptVisitor(final WorkstationVisitor subject) {
        subject.register(this);
    }

    @Override
    public final int getRatedCapacity() {
        return getDelegate().getRatedCapacity();
    }

    @Override
    public double getProcessingRate() {
        return getDelegate().getProcessingRate();
    }

    @Override
    public double getAverageConsumption() {
        return getDelegate().getAverageConsumption();
    }
}
