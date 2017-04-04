package be.kuleuven.cs.gridflex.domain.aggregation.reactive;

import be.kuleuven.cs.gridflex.domain.aggregation.AggregationStrategy;
import be.kuleuven.cs.gridflex.domain.aggregation.AggregationStrategyImpl;
import be.kuleuven.cs.gridflex.domain.aggregation.AggregationUtils;
import be.kuleuven.cs.gridflex.domain.aggregation.Aggregator;
import be.kuleuven.cs.gridflex.domain.energy.tso.contractual.BalancingTSO;
import be.kuleuven.cs.gridflex.domain.energy.tso.contractual.ContractualMechanismParticipant;
import be.kuleuven.cs.gridflex.domain.site.SiteFlexAPI;
import be.kuleuven.cs.gridflex.domain.util.FlexTuple;
import be.kuleuven.cs.gridflex.domain.util.data.IntPowerCapabilityBand;
import be.kuleuven.cs.gridflex.simulation.SimulationContext;
import com.google.common.collect.LinkedListMultimap;

import java.util.List;

/**
 * Subclasses the aggregator abstract class to add the behavior of reacting to
 * tso requests directly.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ReactiveMechanismAggregator extends Aggregator
        implements ContractualMechanismParticipant {

    private int currentTarget;
    private LinkedListMultimap<SiteFlexAPI, FlexTuple> currentFlex;
    private int tickcount;
    private final int aggFreq;

    /**
     * Default constructor.
     *
     * @param host
     *            The host to register to.
     */
    public ReactiveMechanismAggregator(final BalancingTSO host) {
        this(host, AggregationStrategyImpl.CARTESIANPRODUCT);
    }

    /**
     * Constructor with all settable params.
     *
     * @param host
     *            The host to register to.
     * @param frequency
     *            The frequency of aggregation (how often...).
     * @param strategy
     *            The strategy to adopt.
     */
    private ReactiveMechanismAggregator(final BalancingTSO host, final int frequency,
            final AggregationStrategy strategy) {
        super(strategy);
        host.registerParticipant(this);
        this.currentTarget = 0;
        this.currentFlex = LinkedListMultimap.create();
        this.aggFreq = frequency;
    }

    /**
     * Default frequency constructor.
     *
     * @param host
     *            The host to register to.
     * @param strategy
     *            The strategy to adopt.
     */
    public ReactiveMechanismAggregator(final BalancingTSO host,
            final AggregationStrategy strategy) {
        this(host, 1, strategy);
    }

    @Override
    public void signalTarget(final int timestep, final int target) {
        this.currentTarget = target;

    }

    @Override
    public void initialize(final SimulationContext context) {

    }

    @Override
    public void tick(final int t) {
        if (tickcount++ % aggFreq == 0) {
            doAggregationStep(t, currentTarget, currentFlex);
        }
    }

    private int findMaxUpInPortfolio() {
        return findmax(FlexTuple.Direction.UP);
    }

    private int findMaxDownInPortfolio() {
        return findmax(FlexTuple.Direction.DOWN);
    }

    private int findmax(final FlexTuple.Direction direction) {
        final LinkedListMultimap<SiteFlexAPI, FlexTuple> flex = LinkedListMultimap
                .create(currentFlex);
        AggregationUtils.filter(flex, direction);
        final LinkedListMultimap<SiteFlexAPI, FlexTuple> sorted = AggregationUtils
                .sort(flex);
        int sum = 0;
        for (final SiteFlexAPI site : sorted.keySet()) {
            final List<FlexTuple> t = sorted.get(site);
            sum += t.get(t.size() - 1).getDeltaP();
        }
        return sum;
    }

    @Override
    public IntPowerCapabilityBand getPowerCapacity() {
        currentFlex = gatherFlexInfo();
        // only call this once.
        final int up = findMaxUpInPortfolio();
        final int down = findMaxDownInPortfolio();
        return IntPowerCapabilityBand.create(down, up);
    }
}
