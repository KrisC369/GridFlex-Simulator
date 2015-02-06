package be.kuleuven.cs.gametheory.experimentation;

import java.util.List;
import java.util.Map;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregationStrategyImpl;
import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.aggregation.ReactiveMechanismAggregator;
import be.kuleuven.cs.flexsim.domain.energy.generation.ConstantOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.generation.WeighedNormalRandomOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingTSO;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTrackerImpl;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.simulation.Simulator;
import be.kuleuven.cs.gametheory.GameInstance;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Represents a game with two possible actions to choose between.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class TwoActionGameExample implements GameInstance<Site, Aggregator> {

    private static final int ACTIONSPACE_SIZE = 2;
    private static final int ACTPAYMENT = 50;
    private final Simulator sim;
    private final List<Site> sites;
    private final List<Aggregator> aggs;
    private final BalancingTSO tso;
    private final List<FinanceTrackerImpl> ft;
    private int count;
    private final Map<Site, Aggregator> choiceMap;
    private final int baseConsumption;
    private final double factor;

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed
     *            The seed for this experiment.
     * @param baselineConsumption
     *            The baseline for the sites consumption. This is used to base
     *            production params on.
     * @param factor
     *            The retribution factor.
     */
    public TwoActionGameExample(int seed, int baselineConsumption, double factor) {
        this.sim = Simulator.createSimulator(500, seed);
        this.aggs = Lists.newArrayList();
        this.sites = Lists.newArrayList();
        this.ft = Lists.newArrayList();
        this.tso = new BalancingTSO();
        this.count = 0;
        this.baseConsumption = baselineConsumption;
        this.choiceMap = Maps.newLinkedHashMap();
        this.aggs.add(new ReactiveMechanismAggregator(tso,
                AggregationStrategyImpl.CARTESIANPRODUCT));
        this.aggs.add(new ReactiveMechanismAggregator(tso,
                AggregationStrategyImpl.MOVINGHORIZON));
        this.factor = factor;
    }

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed
     *            The seed for this experiment.
     * @param baselineConsumption
     *            The baseline for the sites consumption. This is used to base
     *            production params on.
     */
    public TwoActionGameExample(int seed, int baselineConsumption) {
        this(seed, baselineConsumption, 1);

    }

    @Override
    public Map<Site, Long> getPayOffs() {
        Map<Site, Long> r = Maps.newLinkedHashMap();
        for (int ag = 0; ag < sites.size(); ag++) {
            r.put(sites.get(ag), (long) ft.get(ag).getTotalProfit());
        }
        return r;
    }

    @Override
    public void fixActionToAgent(Site agent, Aggregator action) {
        sites.add(agent);
        tso.registerConsumer(agent);
        choiceMap.put(agent, action);
        FinanceTrackerImpl fti;
        if (aggs.get(0).equals(action)) {
            fti = (FinanceTrackerImpl) FinanceTrackerImpl
                    .createCustomBalancingFeeTracker(agent, ACTPAYMENT,
                            this.factor);
        } else {
            fti = (FinanceTrackerImpl) FinanceTrackerImpl
                    .createCustomBalancingFeeTracker(agent, ACTPAYMENT, 0);
        }
        ft.add(fti);
        sim.register(fti);
        action.registerClient(agent);
        this.count++;
    }

    @Override
    public void start() {
        sim.start();
    }

    @Override
    public void init() {
        // Add finance trackers keeping track of profit and consumptions.
        EnergyProductionTrackable p1 = new ConstantOutputGenerator(
                baseConsumption * getNumberOfAgents());
        EnergyProductionTrackable p2 = new WeighedNormalRandomOutputGenerator(
                -1500, 1500, 0.010);
        for (Aggregator agg : this.aggs) {
            sim.register(agg);
        }
        tso.registerProducer(p1);
        tso.registerProducer(p2);
        sim.register(tso);
    }

    private int getNumberOfAgents() {
        return count;
    }

    @Override
    public List<Aggregator> getActionSet() {
        return Lists.newArrayList(aggs);
    }

    @Override
    public Map<Site, Aggregator> getAgentToActionMapping() {
        return Maps.newLinkedHashMap(choiceMap);
    }

    /**
     * @return the actionspacesize
     */
    public static final int getActionspacesize() {
        return ACTIONSPACE_SIZE;
    }
}
