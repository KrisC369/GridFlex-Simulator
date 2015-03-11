package be.kuleuven.cs.flexsim.experimentation.saso;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.aggregation.brp.BRPAggregator;
import be.kuleuven.cs.flexsim.domain.aggregation.brp.PriceSignal;
import be.kuleuven.cs.flexsim.domain.energy.generation.ConstantOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.generation.WeighedNormalRandomOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.tso.random.RandomTSO;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTracker;
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
public class RenumerationGame implements GameInstance<Site, BRPAggregator> {

    private static final int ACTIONSPACE_SIZE = 2;
    private static final int ACTPAYMENT = 50;
    private final Simulator sim;
    private final List<Site> sites;
    private final List<BRPAggregator> aggs;
    private final RandomTSO tso;
    private final List<FinanceTracker> ft;
    private int count;
    private final Map<Site, BRPAggregator> choiceMap;
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
    public RenumerationGame(int seed, int baselineConsumption, double factor) {
        this.sim = Simulator.createSimulator(500, seed);
        this.aggs = Lists.newArrayList();
        this.sites = Lists.newArrayList();
        this.ft = Lists.newArrayList();
        this.tso = new RandomTSO(-200, 200, new MersenneTwister(seed));
        this.count = 0;
        this.baseConsumption = baselineConsumption;
        this.choiceMap = Maps.newLinkedHashMap();
        this.aggs.add(new BRPAggregator(tso, new PriceSignal() {

            @Override
            public int getCurrentPrice() {
                return 10;
            }
        }, 0, 1));
        this.aggs.add(new BRPAggregator(tso, new PriceSignal() {

            @Override
            public int getCurrentPrice() {
                return 10;
            }
        }, factor, 1 - factor));
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
    public RenumerationGame(int seed, int baselineConsumption) {
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
    public void fixActionToAgent(Site agent, BRPAggregator action) {
        sites.add(agent);
        // tso.registerConsumer(agent);
        choiceMap.put(agent, action);
        FinanceTracker fti;
        action.registerClient(agent);
        fti = action.getFinanceTrackerFor(agent);
        // if (aggs.get(0).equals(action)) {
        // fti = (FinanceTrackerImpl) FinanceTrackerImpl
        // .createCustomBalancingFeeTracker(agent, ACTPAYMENT,
        // this.factor);
        // } else {
        // fti = (FinanceTrackerImpl) FinanceTrackerImpl
        // .createCustomBalancingFeeTracker(agent, ACTPAYMENT, 0);
        // }
        ft.add(fti);
        sim.register((FinanceTrackerImpl) fti);
        this.count++;
    }

    @Override
    public void play() {
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
        // tso.registerProducer(p1);
        // tso.registerProducer(p2);
        // sim.register(tso);
    }

    private int getNumberOfAgents() {
        return count;
    }

    @Override
    public List<BRPAggregator> getActionSet() {
        return Lists.newArrayList(aggs);
    }

    @Override
    public Map<Site, BRPAggregator> getAgentToActionMapping() {
        return Maps.newLinkedHashMap(choiceMap);
    }

    /**
     * @return the actionspacesize
     */
    public static final int getActionspacesize() {
        return ACTIONSPACE_SIZE;
    }
}
