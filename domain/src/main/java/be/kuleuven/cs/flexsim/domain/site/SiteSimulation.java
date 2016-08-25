package be.kuleuven.cs.flexsim.domain.site;

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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Class representing a site module that makes abstraction of the underlying
 * mechanism and produces flex and consumption patterns.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class SiteSimulation implements Site {

    private final int maxLimitConsumption;
    private final int minLimitConsumption;
    private final int maxTuples;
    private final int ramp;
    private final int cease;
    private final int duration;
    private int currentConsumption;
    private List<FlexTuple> flexData;
    private UIDGenerator generator = () -> 0;
    private int totalConsumption;
    private final int baseProduction;
    private Listener<? super FlexTuple> activationListener;
    private static final int DEFAULT_BASE_PRODUCTION = 20;
    private final int baseConsumption;
    private int noFlexTimer;
    private int flexTimer;

    SiteSimulation(final int base, final int min, final int max, final int maxTuples) {
        this(base, min, max, maxTuples, 1, 0, 0);
    }

    /**
     * Default constructor for this mock simulating site.
     *
     * @param base      The base consumption to start from.
     * @param min       The minimum limit for consumption.
     * @param max       The maximum limit for consumption.
     * @param maxTuples The maximum tuples to generate per section of flex.
     * @param duration  the duration of flex profiles.
     * @param ramp      the ramp up time for activation.
     * @param cease     the cease time for activation.
     */
    SiteSimulation(final int base, final int min, final int max, final int maxTuples,
            final int duration,
            final int ramp, final int cease) {
        checkArgument(min <= base && base <= max);
        this.activationListener = NoopListener.INSTANCE;
        this.maxLimitConsumption = max;
        this.minLimitConsumption = min;
        this.maxTuples = maxTuples;
        this.currentConsumption = base;
        this.flexData = Lists.newArrayList();
        this.baseProduction = DEFAULT_BASE_PRODUCTION;
        this.noFlexTimer = 0;
        this.flexTimer = 0;
        this.baseConsumption = base;
        this.ramp = ramp;
        this.cease = cease;
        this.duration = duration;
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
    public void activateFlex(final ActivateFlexCommand schedule) {
        for (final FlexTuple f : flexData) {
            if (f.getId() == schedule.getReferenceID()) {
                if (!f.getDirection().booleanRepresentation()) {
                    currentConsumption -= f.getDeltaP();
                } else {
                    currentConsumption += f.getDeltaP();
                }
                startTheClock(f.getT(), f.getTC());
                this.activationListener.eventOccurred(f);
            }
        }
    }

    private void startTheClock(final int steps, final int cease) {
        this.noFlexTimer = steps + cease;
        this.flexTimer = steps;
    }

    @Override
    public List<Integer> getBufferOccupancyLevels() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Resource> takeResources() {
        final List<Resource> res = Lists.newArrayList();
        final double factor = (double) (getCurrentConsumption()
                - getMinLimitConsumption())
                / (double) (getMaxLimitConsumption()
                - getMinLimitConsumption());

        for (int i = 0; i < Math.ceil(factor * baseProduction); i++) {
            res.add(ResourceFactory.createResource(0));
        }
        return res;
    }

    @Override
    public void deliverResources(final List<Resource> res) {
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
    public void afterTick(final int t) {
        updateConsumption();
    }

    private void updateConsumption() {
        this.totalConsumption += getCurrentConsumption();
    }

    protected void calculateCurrentFlex() {
        final List<FlexTuple> upFlex = Lists.newArrayList();
        final List<FlexTuple> downFlex = Lists.newArrayList();
        // upFlex:
        final int upDiff = getMaxLimitConsumption() - getCurrentConsumption();
        int diffStep = upDiff / getMaxTuples();
        for (int i = 1; i <= getMaxTuples(); i++) {
            upFlex.add(makeTuple(i * diffStep, true));
        }
        final int downDiff = getCurrentConsumption() - getMinLimitConsumption();
        diffStep = downDiff / getMaxTuples();
        for (int i = 1; i <= getMaxTuples(); i++) {
            downFlex.add(makeTuple(i * diffStep, false));
        }
        resetFlex(upFlex, downFlex);
    }

    protected final FlexTuple makeTuple(final int power, final boolean isUpflex) {
        return FlexTuple
                .create(newId(), power, FlexTuple.Direction.fromRepresentation(isUpflex), duration,
                        ramp,
                        cease);
    }

    private long newId() {
        return generator.getNextUID();
    }

    protected void resetFlex(final List<FlexTuple> upFlex, final List<FlexTuple> downFlex) {
        this.flexData = Lists.newArrayList(upFlex);
        this.flexData.addAll(downFlex);
    }

    @Override
    public void tick(final int t) {
        if (flexTimer == 0) {
            resetConsumption();
        }
        if (flexTimer > 0) {
            flexTimer--;
        }
        if (noFlexTimer > 0) {
            noFlexTimer--;
        }
    }

    private void resetConsumption() {
        this.currentConsumption = baseConsumption;
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    @Override
    public void initialize(final SimulationContext context) {
        this.generator = context.getUIDGenerator();
    }

    @Override
    public boolean containsLine(final FlexProcess process) {
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(35);
        builder.append("SiteSimulation [#T=").append(maxTuples).append(", hc=")
                .append(hashCode()).append(", cCons=")
                .append(currentConsumption).append("]");
        return builder.toString();
    }

    @Override
    public void addActivationListener(final Listener<? super FlexTuple> listener) {
        this.activationListener = MultiplexListener
                .plus(this.activationListener, listener);

    }
}
