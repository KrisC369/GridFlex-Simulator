package domain.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import simulation.ISimulationComponent;
import simulation.ISimulationContext;
import domain.Buffer;
import domain.IResource;
import domain.workstation.IWorkstation;
import domain.workstation.Workstation;

/**
 * A productionline representing buffers and workstations.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class ProductionLine implements ISimulationComponent {

    private final List<Buffer<IResource>> buffers;
    private final List<IWorkstation> workstations;

    private ProductionLine() {
        this.buffers = new ArrayList<>();
        this.workstations = new ArrayList<>();
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
        line.workstations.add(Workstation.createConsuming(bIn, bOut, 1, 3));
        line.buffers.add(bOut);
        return line;
    }

    /**
     * Creates a productionline with a more complex layout. O-XXX-O-X-O with O
     * as buffers and X as stations and XXX as parallel stations.
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
        line.workstations.add(Workstation.createConsuming(bIn, b2, 1, 3));
        line.workstations.add(Workstation.createConsuming(bIn, b2, 1, 3));
        line.workstations.add(Workstation.createConsuming(bIn, b2, 1, 3));
        line.workstations.add(Workstation.createConsuming(b2, bOut, 1, 3));
        return line;
    }

    @Override
    public void initialize(ISimulationContext context) {
        for (IWorkstation w : workstations) {
            context.register(w);
        }
    }

    @Override
    public void tick() {
    }

    /**
     * Returns the number of workstations in this line.
     * 
     * @return The number of workstations in this line.
     */
    public int getNumberOfWorkstations() {
        return this.workstations.size();
    }

    /**
     * Take all the processed resources from the end of the line.
     * 
     * @return the processed resources.
     */
    public Collection<IResource> takeResources() {
        return buffers.get(buffers.size() - 1).pullAll();

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
}
