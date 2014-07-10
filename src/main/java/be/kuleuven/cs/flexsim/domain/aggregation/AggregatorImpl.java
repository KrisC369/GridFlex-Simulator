package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import be.kuleuven.cs.flexsim.domain.site.ActivateFlexCommand;
import be.kuleuven.cs.flexsim.domain.site.FlexTuple;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.tso.SteeringSignal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Represents an energy aggregator implementation
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public class AggregatorImpl {
    private Set<SiteFlexAPI> clients;
    private SteeringSignal tso;

    /**
     * Default constructor
     *
     * @param tso
     *            the tso.
     */
    public AggregatorImpl(SteeringSignal tso) {
        this.clients = Sets.newLinkedHashSet();
        this.tso = tso;
    }

    /**
     * @return the clients
     */
    final List<SiteFlexAPI> getClients() {
        return Lists.newArrayList(clients);
    }

    /**
     * @return the tso
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
        ArrayListMultimap<SiteFlexAPI, FlexTuple> flex = gatherFlexInfo();
        Map<Integer, Integer> flexFiltered = filterAndTransform(flex);
        final int target = getTargetFlex();
        int current = 0;
        Set<Integer> ids = Sets.newHashSet();
        for (Entry<Integer, Integer> e : flexFiltered.entrySet()) {
            if (diff(current + e.getValue(), target) < diff(current, target)) {
                current += e.getValue();
                ids.add(e.getKey());
            }
        }
        dispatchActivation(flex, ids);

    }

    private void dispatchActivation(
            ArrayListMultimap<SiteFlexAPI, FlexTuple> flex, Set<Integer> ids) {

        for (SiteFlexAPI s : flex.keySet()) {
            for (int i : ids) {
                Collection<FlexTuple> coll = flex.get(s);
                for (FlexTuple t : coll) {
                    if (t.getId() == i) {
                        s.activateFlex(new ActivateFlexCommand(){});
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

    private Map<Integer, Integer> filterAndTransform(
            ArrayListMultimap<SiteFlexAPI, FlexTuple> flex) {
        Map<Integer, Integer> res = Maps.newHashMap();
        for (FlexTuple f : flex.values()) {
            if (f.getDirection()) {
                res.put(f.getId(), f.getDeltaP());
            } else {
                res.put(f.getId(), f.getDeltaP() * -1);
            }
        }
        return res;
    }

    private ArrayListMultimap<SiteFlexAPI, FlexTuple> gatherFlexInfo() {
        ArrayListMultimap<SiteFlexAPI, FlexTuple> res = ArrayListMultimap
                .create();
        for (SiteFlexAPI s : this.clients) {
            res.putAll(s, s.getFlexTuples());
        }
        return res;
    }
}
