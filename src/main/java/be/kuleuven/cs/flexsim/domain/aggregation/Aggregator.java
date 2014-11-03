package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.site.ActivateFlexCommand;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * An abstract aggregator instance with logic to perform aggregation functions.
 * This class needs to be subclassed to specify how and when to trigger
 * aggregation.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
abstract class Aggregator {
    private final Set<SiteFlexAPI> clients;
    private final AggregationStrategy strategy;

    /**
     * Constructor with custom aggregation strategy.
     *
     * @param strategy
     *            The aggregation strategy to use.
     */
    public Aggregator(AggregationStrategy strategy) {
        this.clients = Sets.newLinkedHashSet();
        this.strategy = strategy;
    }

    /**
     * Default constructor with default aggregation strategy: Cartesianproduct.
     *
     */
    public Aggregator() {
        this(AggregationStrategyImpl.CARTESIANPRODUCT);
    }

    /**
     * @return the clients.
     */
    final List<SiteFlexAPI> getClients() {
        return Lists.newArrayList(clients);
    }

    /**
     * Register a client to this aggregator.
     *
     * @param client
     *            the client should expose the siteflex api service.
     */
    public void registerClient(SiteFlexAPI client) {
        clients.add(client);
    }

    protected void doAggregationStep(int t, final int target) {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> flex = gatherFlexInfo();
        logStep(t, target);
        this.strategy.performAggregationStep(new AggregationDispatch(), t,
                flex, target);
    }

    private LinkedListMultimap<SiteFlexAPI, FlexTuple> gatherFlexInfo() {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> res = LinkedListMultimap
                .create();
        for (SiteFlexAPI s : this.clients) {
            res.putAll(s, s.getFlexTuples());
        }
        return res;
    }

    private void logStep(int t, int target) {
        LoggerFactory
                .getLogger(IndependentAggregator.class)
                .debug("Performing aggregation step at time step {} with flextarget {}",
                        t, target);
    }

    private void logCurtail(FlexTuple tt) {
        LoggerFactory.getLogger(IndependentAggregator.class).debug(
                "Sending curtail request based on profile {}", tt);
    }

    private void logRestore(FlexTuple tt) {
        LoggerFactory.getLogger(IndependentAggregator.class).debug(
                "Sending restore request based on profile {}", tt);
    }

    private class AggregationDispatch implements AggregationContext {

        @Override
        public void dispatchActivation(
                LinkedListMultimap<SiteFlexAPI, FlexTuple> flex, Set<Long> ids) {
            for (SiteFlexAPI s : flex.keySet()) {
                for (long i : ids) {
                    for (FlexTuple t : flex.get(s)) {
                        if (t.getId() == i) {
                            final FlexTuple tt = t;
                            if (tt.getDirection()) {
                                logRestore(tt);
                            } else {
                                logCurtail(tt);
                            }
                            s.activateFlex(new ActivateFlexCommand() {
                                @Override
                                public long getReferenceID() {
                                    return tt.getId();
                                }

                                @Override
                                public boolean isDownFlexCommand() {
                                    return !tt.getDirection();
                                }
                            });
                        }
                    }
                }
            }
        }
    }
}
