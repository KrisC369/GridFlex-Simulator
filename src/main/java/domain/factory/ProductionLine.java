package domain.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import simulation.ISimulationComponent;
import simulation.ISimulationContext;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

import domain.resource.IResource;
import domain.util.Buffer;
import domain.util.SimpleEventFactory;
import domain.workstation.IWorkstation;
import domain.workstation.Workstation;

/**
 * A productionline representing buffers and workstations.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class ProductionLine implements ISimulationComponent {

    private static final int WORKING_CONSUMPTION = 3;
    private static final int IDLE_CONSUMPTION = 1;

    /**
     * Creates a productionline with a more complex layout.
     * <code>O-XXX-O-X-O</code> with O as buffers and X as stations and
     * <code>XXX</code> as parallel stations.
     * 
     * @return A productionline instance.
     */
    public static ProductionLine createExtendedLayout() {
        ProductionLine line = new ProductionLine();
        Buffer<IResource> bIn = new Buffer<>();
        Buffer<IResource> b2 = new Buffer<>();
        Buffer<IResource> bOut = new Buffer<>();
        line.buffers.add(bIn);
        line.buffers.add(b2);
        line.buffers.add(bOut);
        line.workstations.add(Workstation.createConsuming(bIn, b2,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        line.workstations.add(Workstation.createConsuming(bIn, b2,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        line.workstations.add(Workstation.createConsuming(bIn, b2,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        line.workstations.add(Workstation.createConsuming(b2, bOut,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        return line;
    }

    /**
     * Creates a productionline with a simple layout. O-X-O with O as buffers
     * and X as stations.
     * 
     * @return A productionline instance.
     */
    public static ProductionLine createSimpleLayout() {
        ProductionLine line = new ProductionLine();
        Buffer<IResource> bIn = new Buffer<>();
        Buffer<IResource> bOut = new Buffer<>();
        line.buffers.add(bIn);
        line.workstations.add(Workstation.createConsuming(bIn, bOut,
                IDLE_CONSUMPTION, WORKING_CONSUMPTION));
        line.buffers.add(bOut);
        return line;
    }

    private final List<Buffer<IResource>> buffers;
    private final List<IWorkstation> workstations;

    private Optional<SimpleEventFactory> eventFac;

    private Optional<EventBus> bus;

    private ProductionLine() {
        this.buffers = new ArrayList<>();
        this.workstations = new ArrayList<>();
        eventFac = Optional.absent();
        bus = Optional.absent();
        ;
    }

    @Override
    public void afterTick() {
        long totalLaststep = 0;
        long totalTotal = 0;
        for (IWorkstation w : workstations) {
            totalLaststep += w.getLastStepConsumption();
            totalTotal += w.getTotalConsumption();
        }
        notifyConsumption(totalLaststep, totalTotal);
    }

    /**
     * Deliver all the resources and use it as input for the line.
     * 
     * @param res
     *            the resources to use.
     */
    public void deliverResources(List<IResource> res) {
        buffers.get(0).pushAll(res);
    }

    /**
     * Returns the number of workstations in this line.
     * 
     * @return The number of workstations in this line.
     */
    public int getNumberOfWorkstations() {
        return this.workstations.size();
    }

    @Override
    public void initialize(ISimulationContext context) {
        for (IWorkstation w : workstations) {
            context.register(w);
        }
        this.eventFac = Optional.of(context.getEventFactory());
        this.bus = Optional.of(context.getEventbus());
    }

    private void notifyConsumption(long totalLaststep, long totalTotal) {
        if (eventFac.isPresent() && bus.isPresent()) {
            Event e = eventFac.get().build("report");
            e.setAttribute("totalLaststepE", totalLaststep);
            e.setAttribute("totalTotalE", totalTotal);
            bus.get().post(e);
        }
    }

    /**
     * Take all the processed resources from the end of the line.
     * 
     * @return the processed resources.
     */
    public Collection<IResource> takeResources() {
        return buffers.get(buffers.size() - 1).pullAll();

    }

    @Override
    public void tick() {
    }
}
