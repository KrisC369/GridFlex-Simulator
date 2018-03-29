package be.kuleuven.cs.gridflex.experimentation.games;

import be.kuleuven.cs.gridflex.domain.aggregation.Aggregator;
import be.kuleuven.cs.gridflex.domain.finance.FinanceTracker;
import be.kuleuven.cs.gridflex.simulation.InstrumentationComponent;
import be.kuleuven.cs.gridflex.simulation.SimulationComponent;
import be.kuleuven.cs.gridflex.simulation.Simulator;
import be.kuleuven.cs.gametheory.GameInstance;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Representation of a concrete game played by Agents and aggregators.
 *
 * @param <S> The type of agent that plays in this game.
 * @param <A> The concrete aggregator type in this game.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class AggregationGame<S, A extends Aggregator>
        implements GameInstance<S, A> {

    private static final int SIM_LENGTH = 500;
    private final Simulator sim;
    private final List<S> sites;
    private final List<A> aggs;
    private final Map<S, A> choiceMap;
    private final List<FinanceTracker> ft;

    /**
     * Default constructor.
     *
     * @param seed The seed to use for the simulator in this game.
     */
    protected AggregationGame(final int seed) {
        this.sim = Simulator.createSimulator(SIM_LENGTH, seed);
        this.aggs = Lists.newArrayList();
        this.sites = Lists.newArrayList();
        this.choiceMap = Maps.newLinkedHashMap();
        this.ft = Lists.newArrayList();
    }

    @Override
    public void play() {
        sim.start();
    }

    @Override
    public void init() {
        this.aggs.forEach(sim::register);
    }

    @Override
    public final List<A> getActionSet() {
        return Lists.newArrayList(aggs);
    }

    @Override
    public Map<S, Double> getPayOffs() {
        final Map<S, Double> r = Maps.newLinkedHashMap();
        for (int ag = 0; ag < getSites().size(); ag++) {
            r.put(sites.get(ag), ft.get(ag).getTotalProfit());
        }
        return r;
    }

    @Override
    public Map<S, A> getAgentToActionMapping() {
        return Maps.newLinkedHashMap(choiceMap);
    }

    protected final void addAggregator(final A agg) {
        this.aggs.add(agg);
    }

    protected final void addSimComponent(final InstrumentationComponent comp) {
        this.sim.register(comp);
    }

    protected final void addSimComponent(final SimulationComponent comp) {
        this.sim.register(comp);
    }

    protected final void addSite(final S s) {
        this.sites.add(s);
    }

    protected final void addChoice(final S s, final A a) {
        this.choiceMap.put(s, a);
    }

    protected void addFinanceTracker(final FinanceTracker ft) {
        this.ft.add(ft);
        this.addSimComponent(ft);
    }

    /**
     * @return the sites
     */
    protected final List<S> getSites() {
        return Lists.newArrayList(sites);
    }

    /**
     * @return the aggs
     */
    protected final List<A> getAggregators() {
        return Lists.newArrayList(aggs);
    }

    /**
     * @return the choiceMap
     */
    protected final Map<S, A> getChoiceMap() {
        return Maps.newLinkedHashMap(choiceMap);
    }
}