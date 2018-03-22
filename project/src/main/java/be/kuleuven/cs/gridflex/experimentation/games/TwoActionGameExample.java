package be.kuleuven.cs.gridflex.experimentation.games;

import be.kuleuven.cs.gridflex.domain.aggregation.AggregationStrategyImpl;
import be.kuleuven.cs.gridflex.domain.aggregation.Aggregator;
import be.kuleuven.cs.gridflex.domain.aggregation.reactive.ReactiveMechanismAggregator;
import be.kuleuven.cs.gridflex.domain.energy.generation.ConstantOutputGenerator;
import be.kuleuven.cs.gridflex.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.gridflex.domain.energy.generation.WeighedNormalRandomOutputGenerator;
import be.kuleuven.cs.gridflex.domain.energy.tso.contractual.BalancingTSO;
import be.kuleuven.cs.gridflex.domain.finance.FinanceTrackerImpl;
import be.kuleuven.cs.gridflex.domain.site.Site;

/**
 * Represents a game with two possible actions to choose between.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class TwoActionGameExample extends AggregationGame<Site, Aggregator> {

    private static final int ACTIONSPACE_SIZE = 2;
    private static final int ACTPAYMENT = 50;
    private final BalancingTSO tso;
    private int count;
    private final int baseConsumption;
    private final double factor;

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed                The seed for this experiment.
     * @param baselineConsumption The baseline for the sites consumption. This is used to base
     *                            production params on.
     * @param factor              The retribution factor.
     */
    public TwoActionGameExample(final int seed, final int baselineConsumption,
            final double factor) {
        super(seed);
        this.tso = new BalancingTSO();
        this.count = 0;
        this.baseConsumption = baselineConsumption;
        addAggregator(new ReactiveMechanismAggregator(tso,
                AggregationStrategyImpl.CARTESIANPRODUCT));
        addAggregator(new ReactiveMechanismAggregator(tso,
                AggregationStrategyImpl.MOVINGHORIZON));
        this.factor = factor;
    }

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed                The seed for this experiment.
     * @param baselineConsumption The baseline for the sites consumption. This is used to base
     *                            production params on.
     */
    public TwoActionGameExample(final int seed, final int baselineConsumption) {
        this(seed, baselineConsumption, 1);

    }

    @Override
    public void fixActionToAgent(final Site agent, final Aggregator action) {
        addSite(agent);
        tso.registerConsumer(agent);
        addChoice(agent, action);
        final FinanceTrackerImpl fti;
        if (getAggregators().get(0).equals(action)) {
            fti = (FinanceTrackerImpl) FinanceTrackerImpl
                    .createCustomBalancingFeeTracker(agent, ACTPAYMENT,
                            this.factor);
        } else {
            fti = (FinanceTrackerImpl) FinanceTrackerImpl
                    .createCustomBalancingFeeTracker(agent, ACTPAYMENT, 0);
        }
        addFinanceTracker(fti);
        action.registerClient(agent);
        this.count++;
    }

    @Override
    public void init() {
        // Add finance trackers keeping track of profit and consumptions.
        final EnergyProductionTrackable p1 = new ConstantOutputGenerator(
                baseConsumption * getNumberOfAgents());
        final EnergyProductionTrackable p2 = new WeighedNormalRandomOutputGenerator(
                -1500, 1500, 0.010);
        this.getAggregators().forEach(this::addSimComponent);
        tso.registerProducer(p1);
        tso.registerProducer(p2);
        addSimComponent(tso);
    }

    private int getNumberOfAgents() {
        return count;
    }

    /**
     * @return the actionspacesize
     */
    public static final int getActionspacesize() {
        return ACTIONSPACE_SIZE;
    }

    @Override
    public double getExternalityValue() {
        return 0d;
    }
}
