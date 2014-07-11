package be.kuleuven.cs.flexsim.domain.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.util.CollectionUtils;
import be.kuleuven.cs.flexsim.domain.util.IntNNFunction;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.DualModeWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.TradeofSteerableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;
import be.kuleuven.cs.flexsim.domain.workstation.WorkstationFactory;
import be.kuleuven.cs.flexsim.domain.workstation.WorkstationRegisterable;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.collect.Lists;

/**
 * A production line representing buffers and workstations.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class ProductionLine implements TrackableFlexProcessComponent {

    private static final IntNNFunction<Workstation> LASTSTEP_CONSUMPTION = new IntNNFunction<Workstation>() {
        @Override
        public int apply(Workstation input) {
            return (int) input.getLastStepConsumption();
        }
    };

    private static final IntNNFunction<Workstation> TOTAL_CONSUMPTION = new IntNNFunction<Workstation>() {
        @Override
        public int apply(Workstation input) {
            return (int) input.getTotalConsumption();
        }
    };

    private final List<Buffer<Resource>> buffers;
    private final List<Workstation> workstations;
    private final Set<CurtailableWorkstation> curtailables;
    private final Set<TradeofSteerableWorkstation> steerables;
    private final Set<DualModeWorkstation> duals;
    private final Set<Workstation> uniques;
    private final PLRegisterable registry;

    private ProductionLine() {
        this.buffers = new ArrayList<>();
        this.workstations = new ArrayList<>();
        this.curtailables = new LinkedHashSet<>();
        this.duals = new LinkedHashSet<>();
        this.steerables = new LinkedHashSet<>();
        this.registry = new PLRegisterable();
        this.uniques = new HashSet<Workstation>();
    }

    @Override
    public List<Integer> getBufferOccupancyLevels() {
        List<Integer> buffSizes = new ArrayList<>();
        for (Buffer<Resource> b : buffers) {
            buffSizes.add(b.getCurrentOccupancyLevel());
        }
        return buffSizes;
    }

    @Override
    public int getAggregatedLastStepConsumptions() {
        return CollectionUtils.sum(workstations, LASTSTEP_CONSUMPTION);
    }

    @Override
    public int getAggregatedTotalConsumptions() {
        return CollectionUtils.sum(workstations, TOTAL_CONSUMPTION);
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        return new ArrayList<SimulationComponent>(this.workstations);
    }

    @Override
    public void tick(int t) {
    }

    @Override
    public void afterTick(int t) {
    }

    /**
     * Deliver all the resources and use it as input for the line.
     * 
     * @param res
     *            the resources to use.
     */
    @Override
    public void deliverResources(List<Resource> res) {
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

    @Override
    public List<FlexTuple> getCurrentFlexbility() {
        if (getCurtailableStations().isEmpty()
                && getSteerableStations().isEmpty()) {
            return Lists.newArrayList(FlexTuple.createNONE());
        }
        List<FlexTuple> flex = Lists.newArrayList();
        for (CurtailableWorkstation c : getCurtailableStations()) {
            flex.add(calculateCurtFlex(c));
        }
        for (TradeofSteerableWorkstation c : getSteerableStations()) {
            flex.add(calculateSteerFlex(c));
        }
        return flex;
    }

    private FlexTuple calculateSteerFlex(TradeofSteerableWorkstation c) {
        // TODO implement
        return FlexTuple.createNONE();
    }

    private FlexTuple calculateCurtFlex(CurtailableWorkstation c) {
        // TODO implement
        return FlexTuple.createNONE();
    }

    /**
     * Builder class for building production line instances.
     * 
     * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
     */
    public static class ProductionLineBuilder {

        private static final int RF_HIGH = 1800;
        private static final int RF_LOW = 300;
        private static final int MULTICAP_WORKING_CONSUMPTION = 2000;
        private static final int IDLE_CONSUMPTION = 100;
        private static final int WORKING_CONSUMPTION = 200;
        private static final int RFWIDTH = 300;
        private int rfWidth = RFWIDTH;
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
            prodline.buffers.add(new Buffer<Resource>());
            workingConsumption = WORKING_CONSUMPTION;
            idleConsumption = IDLE_CONSUMPTION;
            multicapWorkingConsumption = MULTICAP_WORKING_CONSUMPTION;
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
         * @param n
         *            the number of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addShifted(int n) {
            prodline.buffers.add(new Buffer<Resource>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createShiftableWorkstation(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, workingConsumption, i % 2)
                        .registerWith(prodline.registry);
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
                WorkstationFactory.createCurtailableStation(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, workingConsumption, shift)
                        .registerWith(prodline.registry);
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
                WorkstationFactory.createDefault(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1))
                        .registerWith(prodline.registry);
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
                WorkstationFactory.createConsuming(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, workingConsumption).registerWith(
                        prodline.registry);
            }
            return this;
        }

        /**
         * Adds a number of parallel constant energy consuming workstations that
         * can handle multiple items at once, to the line.
         * 
         * @param n
         *            the number of parallel stations
         * @param cap
         *            the capapcity of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addMultiCapConstantConsuming(int n, int cap) {
            prodline.buffers.add(new Buffer<Resource>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createMultiCapConsuming(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, multicapWorkingConsumption, cap)
                        .registerWith(prodline.registry);
            }
            return this;
        }

        /**
         * Adds a number of parallel linear energy consuming workstations that
         * can handle multiple items at once, to the line.
         * 
         * @param n
         *            the number of parallel stations
         * @param cap
         *            the capapcity of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addMultiCapLinearConsuming(int n, int cap) {
            prodline.buffers.add(new Buffer<Resource>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createMultiCapLinearConsuming(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, multicapWorkingConsumption, cap)
                        .registerWith(prodline.registry);

            }
            return this;
        }

        /**
         * Adds a number of parallel exponential energy consuming workstations
         * that can handle multiple items at once, to the line.
         * 
         * @param n
         *            the number of parallel stations
         * @param cap
         *            the capapcity of parallel stations
         * @return the current builder instance
         */
        public ProductionLineBuilder addMultiCapExponentialConsuming(int n,
                int cap) {
            prodline.buffers.add(new Buffer<Resource>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createMultiCapExponentialConsuming(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        idleConsumption, multicapWorkingConsumption, cap)
                        .registerWith(prodline.registry);
            }
            return this;
        }

        /**
         * Adds a number of RFSteerable stations to the line.
         * 
         * @param n
         *            the amount of instances
         * @param cap
         *            the capacity of this station.
         * @return the current builder instance.
         */
        public ProductionLineBuilder addRFSteerableStation(int n, int cap) {
            prodline.buffers.add(new Buffer<Resource>());
            for (int i = 0; i < n; i++) {
                WorkstationFactory.createRFDualModeStation(
                        prodline.buffers.get(prodline.buffers.size() - 2),
                        prodline.buffers.get(prodline.buffers.size() - 1),
                        rfLowConsumption, rfHighConsumption, rfWidth, cap)
                        .registerWith(prodline.registry);
            }
            return this;
        }

        /**
         * @param rfWidth
         *            the rfWidth to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setRfWidth(int rfWidth) {
            this.rfWidth = rfWidth;
            return this;
        }

        /**
         * @param workingConsumption
         *            the workingConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setWorkingConsumption(
                int workingConsumption) {
            this.workingConsumption = workingConsumption;
            return this;
        }

        /**
         * @param idleConsumption
         *            the idleConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setIdleConsumption(
                int idleConsumption) {
            this.idleConsumption = idleConsumption;
            return this;
        }

        /**
         * @param multicapWorkingConsumption
         *            the multicapWorkingConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setMulticapWorkingConsumption(
                int multicapWorkingConsumption) {
            this.multicapWorkingConsumption = multicapWorkingConsumption;
            return this;
        }

        /**
         * @param rfLowConsumption
         *            the rfLowConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setRfLowConsumption(
                int rfLowConsumption) {
            this.rfLowConsumption = rfLowConsumption;
            return this;
        }

        /**
         * @param rfHighConsumption
         *            the rfHighConsumption to set
         * @return this builder instance.
         */
        public final ProductionLineBuilder setRfHighConsumption(
                int rfHighConsumption) {
            this.rfHighConsumption = rfHighConsumption;
            return this;
        }

    }

    private final class PLRegisterable implements WorkstationRegisterable {

        @Override
        public void register(Workstation ws) {
            if (!uniques.contains(ws)) {
                uniques.add(ws);
                workstations.add(ws);
            }
        }

        @Override
        public void register(CurtailableWorkstation ws) {
            curtailables.add(ws);
        }

        @Override
        public void register(TradeofSteerableWorkstation ws) {
            steerables.add(ws);
        }

        @Override
        public void register(DualModeWorkstation ws) {
            duals.add(ws);
        }
    }
}
