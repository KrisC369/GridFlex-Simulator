package be.kuleuven.cs.flexsim.domain.site;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.process.FlexProcess;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;
import be.kuleuven.cs.flexsim.domain.util.listener.MultiplexListener;
import be.kuleuven.cs.flexsim.domain.util.listener.NoopListener;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.flexsim.simulation.UIDGenerator;

import com.google.common.collect.Lists;

/**
 * Class representing a site module that makes abstraction of the underlying
 * mechanism and produces flex and consumption patterns.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class SiteSimulation implements Site {

    private final int maxLimitConsumption;
    private final int minLimitConsumption;
    private final int maxTuples;
    private int currentConsumption;
    private List<FlexTuple> flexData;
    private UIDGenerator generator;
    private int totalConsumption;
    private final int baseProduction;
    private Listener<? super ActivateFlexCommand> activationListener;
    private static final int DEFAULT_BASE_PRODUCTION = 20;
    private final int baseConsumption;
    private int noFlexTimer;
    private int flexTimer;

    /**
     * Default constructor for this mock simulating site.
     * 
     * @param base
     *            The base consumption to start from.
     * @param min
     *            The minimum limit for consumption.
     * @param max
     *            The maximum limit for consumption.
     * @param maxTuples
     *            The maximum tuples to generate per section of flex.
     */
    SiteSimulation(int base, int min, int max, int maxTuples) {
        checkArgument(min <= base && base <= max);
        this.activationListener = NoopListener.INSTANCE;
        this.maxLimitConsumption = max;
        this.minLimitConsumption = min;
        this.maxTuples = maxTuples;
        this.currentConsumption = base;
        this.flexData = Lists.newArrayList();
        this.baseProduction = DEFAULT_BASE_PRODUCTION;
        this.generator = new UIDGenerator() {
            @Override
            public long getNextUID() {
                return 0;
            }
        };
        this.noFlexTimer = 0;
        this.flexTimer = 0;
        this.baseConsumption = base;
    }

    @Override
    public List<FlexTuple> getFlexTuples() {
        if (this.noFlexTimer == 0) {
            calculateCurrentFlex();
            return Lists.newArrayList(flexData);
        }
        return Lists.newArrayList();
    }

    @Override
    public void activateFlex(ActivateFlexCommand schedule) {
        for (FlexTuple f : flexData) {
            if (f.getId() == schedule.getReferenceID()) {
                if (schedule.isDownFlexCommand()) {
                    currentConsumption -= f.getDeltaP();
                } else {
                    currentConsumption += f.getDeltaP();
                }
                startTheClock(f.getT(), f.getTR());
                this.activationListener.eventOccurred(schedule);
            }
        }
    }

    private void startTheClock(int steps, int cease) {
        this.noFlexTimer = steps + cease;
        this.flexTimer = steps;
    }

    @Override
    public List<Integer> getBufferOccupancyLevels() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Resource> takeResources() {
        List<Resource> res = Lists.newArrayList();
        double factor = (double) (getCurrentConsumption() - getMinLimitConsumption())
                / (double) (getMaxLimitConsumption() - getMinLimitConsumption());

        for (int i = 0; i < Math.ceil(factor * baseProduction); i++) {
            res.add(ResourceFactory.createResource(0));
        }
        return res;
    }

    @Override
    public void deliverResources(List<Resource> res) {
        throw new UnsupportedOperationException(
                "This implementation does not support resource handling stuff.");
    }

    @Override
    public double getLastStepConsumption() {
        return getCurrentConsumption();
    }

    @Override
    public double getTotalConsumption() {
        return totalConsumption;
    }

    @Override
    public double getAverageConsumption() {
        return getCurrentConsumption();
    }

    @Override
    public void afterTick(int t) {
        updateConsumption();
    }

    private void updateConsumption() {
        this.totalConsumption += getCurrentConsumption();
    }

    protected void calculateCurrentFlex() {
        List<FlexTuple> upFlex = Lists.newArrayList();
        List<FlexTuple> downFlex = Lists.newArrayList();
        // upFlex:
        int upDiff = getMaxLimitConsumption() - getCurrentConsumption();
        int diffStep = upDiff / getMaxTuples();
        for (int i = 1; i <= getMaxTuples(); i++) {
            upFlex.add(makeTuple(i * diffStep, true));
        }
        int downDiff = getCurrentConsumption() - getMinLimitConsumption();
        diffStep = downDiff / getMaxTuples();
        for (int i = 1; i <= getMaxTuples(); i++) {
            downFlex.add(makeTuple(i * diffStep, false));
        }
        resetFlex(upFlex, downFlex);
    }

    protected final FlexTuple makeTuple(int power, boolean isUpflex) {
        // return FlexTuple.create(newId(), power, isUpflex,
        // (int) (Math.random() * 50), 0, (int) (Math.random() * 10));
        return FlexTuple.create(newId(), power, isUpflex, 0, 0, 0);
    }

    private long newId() {
        return generator.getNextUID();
    }

    protected void resetFlex(List<FlexTuple> upFlex, List<FlexTuple> downFlex) {
        this.flexData = Lists.newArrayList(upFlex);
        this.flexData.addAll(downFlex);
    }

    @Override
    public void tick(int t) {
        if (flexTimer > 0) {
            flexTimer--;
        }
        if (flexTimer == 0) {
            resetCons();
        }
        if (noFlexTimer > 0) {
            noFlexTimer--;
        }
    }

    private void resetCons() {
        this.currentConsumption = baseConsumption;
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    @Override
    public void initialize(SimulationContext context) {
        this.generator = context.getUIDGenerator();
    }

    @Override
    public boolean containsLine(FlexProcess process) {
        return false;
    }

    /**
     * @return the currentConsumption
     */
    protected final int getCurrentConsumption() {
        return currentConsumption;
    }

    /**
     * @return the maxLimitConsumption
     */
    protected final int getMaxLimitConsumption() {
        return maxLimitConsumption;
    }

    /**
     * @return the minLimitConsumption
     */
    protected final int getMinLimitConsumption() {
        return minLimitConsumption;
    }

    /**
     * @return the maxTuples
     */
    protected final int getMaxTuples() {
        return maxTuples;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SiteSimulation [#T=").append(maxTuples).append(", hc=")
                .append(hashCode()).append(", cCons=")
                .append(currentConsumption).append("]");
        return builder.toString();
    }

    @Override
    public void addActivationListener(
            Listener<? super ActivateFlexCommand> listener) {
        this.activationListener = MultiplexListener.plus(
                this.activationListener, listener);

    }

    /**
     * Default constructor for this mock simulating site.
     * 
     * @param base
     *            The base consumption to start from.
     * @param min
     *            The minimum limit for consumption.
     * @param max
     *            The maximum limit for consumption.
     * @param maxTuples
     *            The maximum tuples to generate per section of flex.
     * @return A new SiteSimulation object.
     */
    public static SiteSimulation createDefault(int base, int min, int max,
            int maxTuples) {
        return new SiteSimulation(base, min, max, maxTuples);

    }

    /**
     * Constructor for a simulating site with equidistant flex.
     * 
     * @param base
     *            The base consumption to start from.
     * @param min
     *            The minimum limit for consumption.
     * @param max
     *            The maximum limit for consumption.
     * @param maxTuples
     *            The maximum tuples to generate per section of flex. * @return
     *            A new SiteSimulation object.
     * @return A new SiteSimulation object.
     */
    public static SiteSimulation createEquidistantFlex(int base, int min,
            int max, int maxTuples) {
        return new EquidistantSiteSimulation(base, min, max, maxTuples);

    }

}
