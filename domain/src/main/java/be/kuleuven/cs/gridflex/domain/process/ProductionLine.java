package be.kuleuven.cs.gridflex.domain.process;

import be.kuleuven.cs.gridflex.domain.resource.Resource;
import be.kuleuven.cs.gridflex.domain.util.Buffer;
import be.kuleuven.cs.gridflex.domain.util.CollectionUtils;
import be.kuleuven.cs.gridflex.domain.util.FlexTuple;
import be.kuleuven.cs.gridflex.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.gridflex.domain.workstation.DualModeWorkstation;
import be.kuleuven.cs.gridflex.domain.workstation.TradeofSteerableWorkstation;
import be.kuleuven.cs.gridflex.domain.workstation.Workstation;
import be.kuleuven.cs.gridflex.domain.workstation.WorkstationFactory;
import be.kuleuven.cs.gridflex.domain.workstation.WorkstationVisitor;
import be.kuleuven.cs.gridflex.simulation.SimulationComponent;
import be.kuleuven.cs.gridflex.simulation.SimulationContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * A production line representing buffers and workstations.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class ProductionLine implements FlexProcess {

    private static final ToIntFunction<Workstation> LASTSTEP_CONSUMPTION = input -> (int) input
            .getLastStepConsumption();

    private static final ToIntFunction<Workstation> TOTAL_CONSUMPTION = input -> (int) input
            .getTotalConsumption();

    private final List<Buffer<Resource>> buffers;
    private final List<Workstation> workstations;
    private final Set<CurtailableWorkstation> curtailables;
    private final Set<TradeofSteerableWorkstation> steerables;
    private final Set<DualModeWorkstation> duals;
    private final Set<Workstation> uniques;
    private final PLRegisterable registry;
    private final Graph<Buffer<Resource>, Workstation> layout;
    private final ProcessDevice flexProcessor;

    ProductionLine() {
        this.buffers = Lists.newArrayList();
        this.workstations = Lists.newArrayList();
        this.curtailables = Sets.newLinkedHashSet();
        this.duals = Sets.newLinkedHashSet();
        this.steerables = Sets.newLinkedHashSet();
        this.registry = new PLRegisterable();
        this.uniques = Sets.newLinkedHashSet();
        this.layout = new SparseMultigraph<>();
        this.flexProcessor = new ProcessDeviceImpl();
    }

    @Override
    public List<Integer> getBufferOccupancyLevels() {
        final List<Integer> buffSizes = new ArrayList<>();
        for (final Buffer<Resource> b : buffers) {
            buffSizes.add(b.getCurrentOccupancyLevel());
        }
        return buffSizes;
    }

    @Override
    public double getLastStepConsumption() {
        return CollectionUtils.sum(workstations, LASTSTEP_CONSUMPTION);
    }

    @Override
    public double getTotalConsumption() {
        return CollectionUtils.sum(workstations, TOTAL_CONSUMPTION);
    }

    @Override
    public void initialize(final SimulationContext context) {
        this.flexProcessor
                .addFlexAspect(new FlexAspectImpl.SingleStationDownFlex(
                        context.getUIDGenerator(), layout))
                .addFlexAspect(new FlexAspectImpl.TwoStationsDownFlex(
                        context.getUIDGenerator(), layout))
                .addFlexAspect(new FlexAspectImpl.ThreeStationsDownFlex(
                        context.getUIDGenerator(), layout))
                .addFlexAspect(new FlexAspectImpl.UpFlex(
                        context.getUIDGenerator(), layout))
                .addFlexAspect(new FlexAspectImpl.SteerFlex(
                        context.getUIDGenerator(), layout))
                .addFlexAspect(new FlexAspectImpl.DualModeFlex(
                        context.getUIDGenerator(), layout));
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        return new ArrayList<>(this.workstations);
    }

    @Override
    public void tick(final int t) {
        getFlexProcessor().invalidate();
    }

    /**
     * Deliver all the resources and use it as input for the line.
     *
     * @param res the resources to use.
     */
    @Override
    public void deliverResources(final List<Resource> res) {
        buffers.get(0).pushAll(res);
    }

    /**
     * Take all the processed resources from the end of the line.
     *
     * @return the processed resources.
     */
    @Override
    public Collection<Resource> takeResources() {
        return buffers.get(buffers.size() - 1).pullAll();
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
     * Creates a production line with a custom layout specified by the
     * arguments.
     *
     * @param initialStations the mandatory first line workstation amount.
     * @param furtherStations further levels of parallel workstations.
     * @return an instantiated production line object adhering to the specified
     * layout.
     */
    public static ProductionLine createCustomLayout(final int initialStations,
            final int... furtherStations) {
        final ProductionLineBuilder b = new ProductionLineBuilder()
                .addShifted(initialStations);
        for (final int furtherStation : furtherStations) {
            b.addShifted(furtherStation);
        }
        return b.build();
    }

    /**
     * Returns the stations that are curtailable in this production line.
     *
     * @return a list of pointers to curtailable instances.
     */
    public List<CurtailableWorkstation> getCurtailableStations() {
        return new ArrayList<>(this.curtailables);
    }

    /**
     * Returns the stations that are dual mode operational in this production
     * line.
     *
     * @return a list of pointers to dual mode instances.
     */
    public List<DualModeWorkstation> getDualModeStations() {
        return new ArrayList<>(this.duals);
    }

    /**
     * Returns the stations that are curtailable in this production line.
     *
     * @return a list of pointers to curtailable instances.
     */
    public List<TradeofSteerableWorkstation> getSteerableStations() {
        return new ArrayList<>(this.steerables);
    }

    /**
     * Returns all workstations in this production line.
     *
     * @return the workstations present.
     */
    List<Workstation> getWorkstations() {
        return new ArrayList<>(this.workstations);
    }

    ProcessDevice getFlexProcessor() {
        return this.flexProcessor;
    }

    @Override
    public List<FlexTuple> getCurrentFlexbility() {
        return getFlexProcessor().getCurrentFlexbility(getCurtailableStations(),
                getSteerableStations(), getDualModeStations());
    }

    @Override
    public void executeDownFlexProfile(final long id) {
        getFlexProcessor().executeDownFlexProfile(id);
    }

    private void addToGraph(final Workstation ws) {
        this.layout.addEdge(ws, buffers.get(buffers.size() - 2),
                buffers.get(buffers.size() - 1), EdgeType.DIRECTED);
    }

    private void addBuffer(final Buffer<Resource> b) {
        this.buffers.add(b);
        this.layout.addVertex(b);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(25);
        builder.append("ProductionLine [layout=").append(layout).append("]");
        return builder.toString();
    }

    /**
     * Returns the layout of this production line instance.
     *
     * @return an unmodifiable graph instance representing the layout.
     */
    public Graph<Buffer<Resource>, Workstation> getLayout() {
        return this.layout;
    }

    @Override
    public void executeUpFlexProfile(final long id) {
        getFlexProcessor().executeUpFlexProfile(id);
    }

    @Override
    public double getAverageConsumption() {
        return getLastStepConsumption();
    }

    /**
     * Builder class for building production line instances.
     *
     * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
     */
    public static class ProductionLineBuilder {

        private static final int RF_HIGH = 1800;
        private static final int RF_LOW = 300;
        private static final int MULTICAP_WORKING_CONSUMPTION = 2000;
        private static final int IDLE_CONSUMPTION = 100;
        private static final int WORKING_CONSUMPTION = 200;
        private static final int RFWIDTH_DEFAULT = 300;
        private int rfWidth;
        private int workingConsumption;
        private int idleConsumption;
        private int multicapWorkingConsumption;
        private int rfLowConsumption;
        private int rfHighConsumption;

        private final ProductionLine prodline;

        /**
         * Default constructor for builder instances.
         */
        public ProductionLineBuilder() {
            prodline = new ProductionLine();
            prodline.addBuffer(new Buffer<>());
            workingConsumption = WORKING_CONSUMPTION;
            idleConsumption = IDLE_CONSUMPTION;
            multicapWorkingConsumption = MULTICAP_WORKING_CONSUMPTION;
            rfWidth = RFWIDTH_DEFAULT;
            rfLowConsumption = RF_LOW;
            rfHighConsumption = RF_HIGH;
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
         * @param n the number of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addShifted(final int n) {
            prodline.addBuffer(new Buffer<>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createShiftableWorkstation(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, workingConsumption, i % 2)
                        .acceptVisitor(prodline.registry);
            }
            return this;
        }

        /**
         * Adds a number of parallel curtailable workstations to the line.
         *
         * @param n the number of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addCurtailableShifted(final int n) {
            prodline.addBuffer(new Buffer<>());
            for (int j = 0; j < n; j++) {
                final int shift = j % 2;
                WorkstationFactory.createCurtailableStation(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, workingConsumption, shift)
                        .acceptVisitor(prodline.registry);
            }
            return this;
        }

        /**
         * * Adds a number of parallel default workstations to the line.
         *
         * @param n the number of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addDefault(final int n) {
            prodline.addBuffer(new Buffer<>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createDefault(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1))
                        .acceptVisitor(prodline.registry);
            }
            return this;
        }

        /**
         * Adds a number of parallel EnergyConsuming workstations to the line.
         *
         * @param n the number of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addConsuming(final int n) {
            prodline.addBuffer(new Buffer<>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createConsuming(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, workingConsumption)
                        .acceptVisitor(prodline.registry);
            }
            return this;
        }

        /**
         * Adds a number of parallel constant energy consuming workstations that
         * can handle multiple items at once, to the line.
         *
         * @param n   the number of parallel stations
         * @param cap the capapcity of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addMultiCapConstantConsuming(final int n,
                final int cap) {
            prodline.addBuffer(new Buffer<>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createMultiCapConsuming(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, multicapWorkingConsumption, cap)
                        .acceptVisitor(prodline.registry);
            }
            return this;
        }

        /**
         * Adds a number of parallel linear energy consuming workstations that
         * can handle multiple items at once, to the line.
         *
         * @param n   the number of parallel stations
         * @param cap the capapcity of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addMultiCapLinearConsuming(final int n,
                final int cap) {
            prodline.addBuffer(new Buffer<>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createMultiCapLinearConsuming(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, multicapWorkingConsumption, cap)
                        .acceptVisitor(prodline.registry);

            }
            return this;
        }

        /**
         * Adds a number of parallel exponential energy consuming workstations
         * that can handle multiple items at once, to the line.
         *
         * @param n   the number of parallel stations
         * @param cap the capapcity of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addMultiCapExponentialConsuming(final int n,
                final int cap) {
            prodline.addBuffer(new Buffer<>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createMultiCapExponentialConsuming(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, multicapWorkingConsumption, cap)
                        .acceptVisitor(prodline.registry);
            }
            return this;
        }

        /**
         * Adds a number of RFSteerable stations to the line.
         *
         * @param n   the amount of instances
         * @param cap the capacity of this station.
         * @return the current builder instance.
         */
        public ProductionLineBuilder addRFSteerableStation(final int n, final int cap) {
            prodline.addBuffer(new Buffer<>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createRFDualModeStation(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        rfLowConsumption, rfHighConsumption, rfWidth, cap)
                        .acceptVisitor(prodline.registry);
            }
            return this;
        }

        /**
         * @param rfWidth the rfWidth to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setRfWidth(final int rfWidth) {
            this.rfWidth = rfWidth;
            return this;
        }

        /**
         * @param workingConsumption the workingConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setWorkingConsumption(
                final int workingConsumption) {
            this.workingConsumption = workingConsumption;
            return this;
        }

        /**
         * @param idleConsumption the idleConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setIdleConsumption(
                final int idleConsumption) {
            this.idleConsumption = idleConsumption;
            return this;
        }

        /**
         * @param multicapWorkingConsumption the multicapWorkingConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setMulticapWorkingConsumption(
                final int multicapWorkingConsumption) {
            this.multicapWorkingConsumption = multicapWorkingConsumption;
            return this;
        }

        /**
         * @param rfLowConsumption the rfLowConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setRfLowConsumption(
                final int rfLowConsumption) {
            this.rfLowConsumption = rfLowConsumption;
            return this;
        }

        /**
         * @param rfHighConsumption the rfHighConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setRfHighConsumption(
                final int rfHighConsumption) {
            this.rfHighConsumption = rfHighConsumption;
            return this;
        }

    }

    private final class PLRegisterable implements WorkstationVisitor {

        @Override
        public void register(final Workstation ws) {
            if (!uniques.contains(ws)) {
                uniques.add(ws);
                workstations.add(ws);
                addToGraph(ws);
            }
        }

        @Override
        public void register(final CurtailableWorkstation ws) {
            curtailables.add(ws);
        }

        @Override
        public void register(final TradeofSteerableWorkstation ws) {
            steerables.add(ws);
        }

        @Override
        public void register(final DualModeWorkstation ws) {
            duals.add(ws);
        }
    }
}
