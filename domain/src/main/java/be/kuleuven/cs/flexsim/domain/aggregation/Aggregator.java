package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Collections;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import be.kuleuven.cs.flexsim.domain.site.ActivateFlexCommand;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;

/**
 * An abstract aggregator instance with logic to perform aggregation functions.
 * This class needs to be subclassed to specify how and when to trigger
 * aggregation.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class Aggregator implements SimulationComponent {
    private final Set<SiteFlexAPI> clients;
    private final AggregationStrategy strategy;
    private final AggregationContext dispatcher;

    /**
     * Constructor with custom aggregation strategy.
     *
     * @param strategy
     *            The aggregation strategy to use.
     */
    public Aggregator(AggregationStrategy strategy) {
        this.clients = Sets.newLinkedHashSet();
        this.strategy = strategy;
        this.dispatcher = new AggregationDispatch();
    }

    /**
     * Default constructor with default aggregation strategy: Cartesianproduct.
     */
    public Aggregator() {
        this(AggregationStrategyImpl.CARTESIANPRODUCT);
    }

    /**
     * Get the clients for this aggregator.
     *
     * @return an Unmodifiable view of the clients of this aggregator.
     */
    @VisibleForTesting
    public final Set<SiteFlexAPI> getClients() {
        return Collections.unmodifiableSet(clients);
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

    protected final int doAggregationStep(int t, final int target,
            Multimap<SiteFlexAPI, FlexTuple> flex) {
        logStep(t, target);
        return this.strategy.performAggregationStep(getAggregationContext(), t,
                flex, target);
    }

    /**
     * Get the Aggregation context specifying the dispatch logic. To be used
     * when calling aggregation strategy implementations.
     *
     * @return The default local dispatching context.
     */
    protected AggregationContext getAggregationContext() {
        return this.dispatcher;
    }

    protected final LinkedListMultimap<SiteFlexAPI, FlexTuple> gatherFlexInfo() {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> res = LinkedListMultimap
                .create();
        for (SiteFlexAPI s : this.clients) {
            res.putAll(s, s.getFlexTuples());
        }
        return res;
    }

    /**
     * @return the strategy
     */
    protected final AggregationStrategy getStrategy() {
        return strategy;
    }

    private void logStep(int t, int target) {
        LoggerFactory.getLogger(Aggregator.class).debug(
                "Performing aggregation step at time step {} with flextarget {}",
                t, target);
    }

    private void logCurtail(FlexTuple tt) {
        LoggerFactory.getLogger(Aggregator.class)
                .debug("Sending curtail request based on profile {}", tt);
    }

    private void logRestore(FlexTuple tt) {
        LoggerFactory.getLogger(Aggregator.class)
                .debug("Sending restore request based on profile {}", tt);
    }

    private class AggregationDispatch implements AggregationContext {

        @Override
        public void dispatchActivation(Multimap<SiteFlexAPI, FlexTuple> flex,
                Set<Long> ids) {
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
                            });
                        }
                    }
                }
            }
        }
    }
}
