/**
 * 
 */
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
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class ExpGameInstance implements GameInstance<Site, Aggregator> {

    // private static final int AGGSTEPS = 1;
    private final Simulator sim;
    private final List<Site> sites;
    private final List<Aggregator> aggs;
    private BalancingTSO tso;
    private final List<FinanceTrackerImpl> ft;
    private final int current = 800, min = 500, max = 1000;
    private int count;
    private Map<Site, Aggregator> choiceMap;

    /**
     * 
     * @param seed
     * @param n1
     * @param n2
     */
    public ExpGameInstance(int seed, int n1, int n2) {
        sim = Simulator.createSimulator(1000, seed);
        aggs = Lists.newArrayList();
        sites = Lists.newArrayList();
        ft = Lists.newArrayList();
        tso = new BalancingTSO();
        this.count = 0;
        this.choiceMap = Maps.newLinkedHashMap();
        this.aggs.add(new ReactiveMechanismAggregator(tso,
                AggregationStrategyImpl.CARTESIANPRODUCT));
        this.aggs.add(new ReactiveMechanismAggregator(tso,
                AggregationStrategyImpl.MOVINGHORIZON));
    }

    @Override
    public Map<Site, Long> getPayOffs() {
        // long[] toRet = new long[sites.size()];
        // for (int i = 0; i < toRet.length; i++) {
        // toRet[i] = (long) ft.get(i).getTotalProfit();
        // }
        Map<Site, Long> r = Maps.newLinkedHashMap();
        for (int ag = 0; ag < sites.size(); ag++) {
            r.put(sites.get(ag), (long) ft.get(ag).getTotalProfit());
        }
        // return toRet;
        return r;
    }

    @Override
    public void fixActionToAgent(Site agent, Aggregator action) {
        sites.add(agent);
        tso.registerConsumer(agent);
        choiceMap.put(agent, action);
        FinanceTrackerImpl fti = (FinanceTrackerImpl) FinanceTrackerImpl
                .createBalancingFeeTracker(agent, 30000);
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
        // for (int i = 0; i < getNumberOfAgents(); i++) {
        // sites.add(SiteSimulation.createDefault(current, min, max, 12));
        // }
        // Deliver resources to these lines.
        // Add finance trackers keeping track of profit and consumptions.
        for (int i = 0; i < getNumberOfAgents(); i++) {
            ft.add((FinanceTrackerImpl) FinanceTrackerImpl
                    .createBalancingFeeTracker((sites.get(i)), 30000));
        }

        // Add the tso with the random signal for the aggregator and the sites
        // connected to it.
        EnergyProductionTrackable p1 = new ConstantOutputGenerator(current
                * getNumberOfAgents());
        EnergyProductionTrackable p2 = new WeighedNormalRandomOutputGenerator(
                -1500, 1500, 0.010);
        tso = new BalancingTSO(sites.toArray(new Site[getNumberOfAgents()]));
        // tso = new CopperplateTSO();
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

}
