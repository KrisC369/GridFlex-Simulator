package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import be.kuleuven.cs.flexsim.domain.site.ActivateFlexCommand;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.tso.SteeringSignal;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Represents an energy aggregator implementation.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class AggregatorImpl implements SimulationComponent {
    private final Set<SiteFlexAPI> clients;
    private final SteeringSignal tso;
    private int tickcount;
    private final int aggFreq;

    /**
     * Default constructor.
     *
     * @param tso
     *            the tso.
     * @param frequency
     *            the frequency with which to perform aggregation functions.
     */
    public AggregatorImpl(SteeringSignal tso, int frequency) {
        this.clients = Sets.newLinkedHashSet();
        this.tso = tso;
        this.tickcount = 1;
        this.aggFreq = frequency;
    }

    /**
     * @return the clients.
     */
    final List<SiteFlexAPI> getClients() {
        return Lists.newArrayList(clients);
    }

    /**
     * @return the tso.
     */
    final SteeringSignal getTso() {
        return tso;
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

    void doAggregationStep() {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> flex = gatherFlexInfo();
        Map<Long, Integer> flexFiltered = filterAndTransform(flex);
        final int target = getTargetFlex();
        int current = 0;
        Set<Long> ids = Sets.newLinkedHashSet();
        for (Entry<Long, Integer> e : flexFiltered.entrySet()) {
            if (diff(current + e.getValue(), target) < diff(current, target)) {
                current += e.getValue();
                ids.add(e.getKey());
            }
        }
        dispatchActivation(flex, ids);

    }

    private void dispatchActivation(
            LinkedListMultimap<SiteFlexAPI, FlexTuple> flex, Set<Long> ids) {
        for (SiteFlexAPI s : flex.keySet()) {
            for (long i : ids) {
                for (FlexTuple t : flex.get(s)) {
                    if (t.getId() == i) {
                        final FlexTuple tt = t;
                        s.activateFlex(new ActivateFlexCommand() {
                            @Override
                            public long getReferenceID() {
                                return tt.getId();
                            }

                            @Override
                            public boolean isCurtailmentCommand() {
                                return !tt.getDirection();
                            }
                        });
                    }
                }
            }
        }
    }

    private int diff(int i, int target) {
        return Math.abs(target - i);
    }

    private int getTargetFlex() {
        return getTso().getCurrentValue(0) * -1;
    }

    private Map<Long, Integer> filterAndTransform(
            LinkedListMultimap<SiteFlexAPI, FlexTuple> flex) {
        Map<Long, Integer> res = Maps.newLinkedHashMap();
        for (FlexTuple f : flex.values()) {
            if (f.getDirection()) {
                res.put(f.getId(), f.getDeltaP());
            } else {
                res.put(f.getId(), f.getDeltaP() * -1);
            }
        }
        return res;
    }

    private LinkedListMultimap<SiteFlexAPI, FlexTuple> gatherFlexInfo() {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> res = LinkedListMultimap
                .create();
        for (SiteFlexAPI s : this.clients) {
            res.putAll(s, s.getFlexTuples());
        }
        return res;
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
            doAggregationStep();
        }
    }

    @Override
    public List<SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }
}
