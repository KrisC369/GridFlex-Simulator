package be.kuleuven.cs.flexsim.domain.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.workstation.Curtailable;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;
import be.kuleuven.cs.flexsim.domain.workstation.WorkstationImpl;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.base.Optional;

/**
 * A production line representing buffers and workstations.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class ProductionLine implements SimulationComponent {

    private static final int BOTTLENECK_NUMBER = 3;
    private static final int CAPACITY_NUMBER = 7;
    private static final int WORKING_CONSUMPTION = BOTTLENECK_NUMBER;
    private static final int IDLE_CONSUMPTION = 1;

    private final List<Buffer<Resource>> buffers;

    private final List<Workstation> workstations;

    private final List<Curtailable> curtailables;

    private Optional<SimulationContext> context;

    private ProductionLine() {
        this.buffers = new ArrayList<>();
        this.workstations = new ArrayList<>();
        this.context = Optional.absent();
        this.curtailables = new ArrayList<>();
    }

    /**
     * This method refines the following documentation by generating a report
     * event when there is simulation context present for this line instance.
     * {@inheritDoc}
     */
    @Override
    public void afterTick() {
        report();
    }

    private void report() {
        long totalLaststep = 0;
        long totalTotal = 0;
        for (Workstation w : workstations) {
            totalLaststep += w.getLastStepConsumption();
            totalTotal += w.getTotalConsumption();
        }
        List<Long> buffSizes = new ArrayList<>();
        for (Buffer<Resource> b : buffers) {
            buffSizes.add((long) b.getCurrentOccupancyLevel());
        }
        notifyReport(totalLaststep, totalTotal, buffSizes);
    }

    /**
     * Deliver all the resources and use it as input for the line.
     * 
     * @param res
     *            the resources to use.
     */
    public void deliverResources(List<Resource> res) {
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
    public void initialize(SimulationContext context) {
        for (Workstation w : workstations) {
            context.register(w);
        }
        this.context = Optional.of(context);
    }

    /**
     * Take all the processed resources from the end of the line.
     * 
     * @return the processed resources.
     */
    public Collection<Resource> takeResources() {
        return buffers.get(buffers.size() - 1).pullAll();
    }

    @Override
    public void tick() {
    }

    private void notifyReport(Long totalLaststep, Long totalTotal,
            List<Long> buffSizes) {
        if (this.context.isPresent()) {
            Event e = getContext().getEventFactory().build("report");
            e.setAttribute("pLinehash", this.hashCode());
            e.setAttribute("time", getContext().getSimulationClock()
                    .getTimeCount());
            e.setAttribute("totalLaststepE", totalLaststep);
            e.setAttribute("totalTotalE", totalTotal);
            int idx = 0;
            for (long i : buffSizes) {
                e.setAttribute("buffer_" + idx++, i);
            }
            getContext().getEventbus().post(e);
        }
    }

    private SimulationContext getContext() {
        return this.context.get();
    }

    /**
     * Creates a production line with a more complex layout.
     * <code>O-XXX-O-X-O</code> with O as buffers and X as stations and
     * <code>XXX</code> as parallel stations.
     * 
     * @return A production line instance.
     */
    public static ProductionLine createExtendedLayout() {
        return createCustomLayout(BOTTLENECK_NUMBER, 1);
    }

    /**
     * Creates a production line with a simple layout. O-X-O with O as buffers
     * and X as stations.
     * 
     * @return A production line instance.
     */
    public static ProductionLine createSimpleLayout() {
        return createCustomLayout(1);
    }

    /**
     * Creates a production line with a more complex layout.
     * <code>O-XXX-O-XX-0-X-O</code> with O as buffers and X as stations and
     * <code>XX..</code> as parallel stations.
     * 
     * @return A production line instance.
     */
    public static ProductionLine createSuperExtendedLayout() {
        return createCustomLayout(BOTTLENECK_NUMBER, BOTTLENECK_NUMBER-1, BOTTLENECK_NUMBER-2);
    }

    /**
     * Creates a production line with a custom layout specified by the
     * arguments.
     * 
     * @param initialStations
     *            the mandatory first line workstation amount.
     * @param furtherStations
     *            further levels of parallel workstations.
     * @return an instantiated production line object adhering to the specified
     *         layout.
     */
    public static ProductionLine createCustomLayout(int initialStations,
            int... furtherStations) {
        ProductionLineBuilder b = new ProductionLineBuilder()
                .addShifted(initialStations);
        for (int i = 0; i < furtherStations.length; i++) {
            b.addShifted(furtherStations[i]);
        }
        return b.build();
    }

    /**
     * Returns the stations that are curtailable in this production line.
     * 
     * @return a list of pointers to curtailable instances.
     */
    public List<Curtailable> getCurtailableStations() {
        return new ArrayList<>(this.curtailables);
    }

    /**
     * Returns a newly created production line with a static layout and some
     * curtailable workstations.
     * 
     * @return a newly created production line instance.
     */
    public static ProductionLine createStaticCurtailableLayout() {
        return new ProductionLineBuilder().addShifted(CAPACITY_NUMBER)
                .addCurtailableShifted(CAPACITY_NUMBER).addShifted(BOTTLENECK_NUMBER).build();
    }

    /**
     * Returns all workstations in this production line.
     * 
     * @return the workstations present.
     */
    List<Workstation> getWorkstations() {
        return new ArrayList<>(this.workstations);
    }

    /**
     * Builder class for building production line instances.
     * 
     * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
     */
    public static class ProductionLineBuilder {

        private ProductionLine prodline = new ProductionLine();

        /**
         * Default constructor for builder instances.
         */
        public ProductionLineBuilder() {
            prodline = new ProductionLine();
            prodline.buffers.add(new Buffer<Resource>());
        }

        /**
         * Builds the configured production line.
         * 
         * @return The production line.
         */
        public ProductionLine build() {
            return prodline;
        }

        /**
         * Adds a number of parallel one-step-shifted workstations to the line.
         * 
         * @param n
         *            the number of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addShifted(int n) {
            prodline.buffers.add(new Buffer<Resource>());
            for (int i = 0; i < n; i++) {
                prodline.workstations
                        .add(WorkstationImpl.createShiftableWorkstation(
                                prodline.buffers
                                        .get(prodline.buffers.size() - 2),
                                prodline.buffers.get(prodline.buffers.size() - 1),
                                IDLE_CONSUMPTION, WORKING_CONSUMPTION, i % 2));
            }
            return this;
        }

        /**
         * Adds a number of parallel curtailable workstations to the line.
         * 
         * @param n
         *            the number of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addCurtailableShifted(int n) {
            prodline.buffers.add(new Buffer<Resource>());
            for (int j = 0; j < n; j++) {
                int shift = j % 2;
                Workstation w = WorkstationImpl.createCurtailableStation(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        IDLE_CONSUMPTION, WORKING_CONSUMPTION, shift);
                prodline.workstations.add(w);
                prodline.curtailables.add((Curtailable) w);

            }
            return this;
        }

        /**
         * * Adds a number of parallel default workstations to the line.
         * 
         * @param n
         *            the number of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addDefault(int n) {
            prodline.buffers.add(new Buffer<Resource>());
            for (int i = 0; i < n; i++) {
                prodline.workstations.add(WorkstationImpl.createDefault(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1)));
            }
            return this;
        }

        /**
         * Adds a number of parallel EnergyConsuming workstations to the line.
         * 
         * @param n
         *            the number of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addConsuming(int n) {
            prodline.buffers.add(new Buffer<Resource>());
            for (int i = 0; i < n; i++) {
                prodline.workstations.add(WorkstationImpl.createConsuming(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        IDLE_CONSUMPTION, WORKING_CONSUMPTION));
            }
            return this;
        }
    }
}
