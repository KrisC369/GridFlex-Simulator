package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingTSO;
import be.kuleuven.cs.flexsim.domain.energy.tso.ContractualMechanismParticipant;
import be.kuleuven.cs.flexsim.domain.energy.tso.PowerCapabilityBand;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.collect.LinkedListMultimap;

/**
 * Subclasses the aggregator abstract class to add the behavior of reacting to
 * tso requests directly.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class ReactiveMechanismAggregator extends Aggregator implements
        ContractualMechanismParticipant, SimulationComponent {

    private int currentTarget;
    private LinkedListMultimap<SiteFlexAPI, FlexTuple> currentFlex;
    private int tickcount;
    private final int aggFreq;

    /**
     * Default constructor
     * 
     * @param host
     *            The host to register to.
     * 
     */
    public ReactiveMechanismAggregator(BalancingTSO host) {
        this(host, AggregationStrategyImpl.CARTESIANPRODUCT);
    }

    // /**
    // * Constructor with all settable params.
    // *
    // * @param host
    // * The host to register to.
    // * @param frequency
    // * The frequency of aggregation (how often...).
    // *
    // */
    // public ReactiveMechanismAggregator(BalancingTSO host, int frequency) {
    // this(host, frequency, AggregationStrategyImpl.CARTESIANPRODUCT);
    // }

    /**
     * Constructor with all settable params.
     * 
     * @param host
     *            The host to register to.
     * @param frequency
     *            The frequency of aggregation (how often...).
     * @param strategy
     *            The strategy to adopt.
     * 
     */
    private ReactiveMechanismAggregator(BalancingTSO host, int frequency,
            AggregationStrategy strategy) {
        super(strategy);
        host.registerParticipant(this);
        this.currentTarget = 0;
        this.currentFlex = LinkedListMultimap.create();
        this.aggFreq = frequency;
    }

    /**
     * Default frequency constructor
     * 
     * @param host
     *            The host to register to.
     * @param strategy
     *            The strategy to adopt.
     */
    public ReactiveMechanismAggregator(BalancingTSO host,
            AggregationStrategy strategy) {
        this(host, 1, strategy);
    }

    @Override
    public void signalTarget(int timestep, int target) {
        this.currentTarget = target;

    }

    @Override
    public void initialize(SimulationContext context) {

    }

    @Override
    public void afterTick(int t) {

    }

    @Override
    public void tick(int t) {
        if (tickcount++ % aggFreq == 0) {
            doAggregationStep(t, currentTarget, currentFlex);
        }
    }

    private int findMaxUpInPortfolio() {
        return findmax(true);
    }

    private int findMaxDownInPortfolio() {
        return findmax(false);
    }

    private int findmax(boolean up) {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> flex = LinkedListMultimap
                .create(currentFlex);
        AggregationStrategyImpl.filter(flex, up);
        LinkedListMultimap<SiteFlexAPI, FlexTuple> sorted = AggregationStrategyImpl
                .sort(flex);
        int sum = 0;
        for (SiteFlexAPI site : sorted.keySet()) {
            List<FlexTuple> t = sorted.get(site);
            sum += t.get(t.size() - 1).getDeltaP();
        }
        return sum;
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    @Override
    public PowerCapabilityBand getPowerCapacity() {
        currentFlex = gatherFlexInfo();// only call this once.
        int up = findMaxUpInPortfolio();
        int down = findMaxDownInPortfolio();
        return PowerCapabilityBand.create(down, up);
    }
}
