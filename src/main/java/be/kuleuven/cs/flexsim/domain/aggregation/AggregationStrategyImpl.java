package be.kuleuven.cs.flexsim.domain.aggregation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.NPermuteAndCombiner;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Implementations of aggregation strategies.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public enum AggregationStrategyImpl implements AggregationStrategy {
    /**
     * An implementation that uses the cartesian product of sets to calculate
     * all possible solutions and then picks the best one. Costly,
     * performancewise.
     */
    CARTESIANPRODUCT() {

        @Override
        public void performAggregationStep(AggregationContext context, int t,
                LinkedListMultimap<SiteFlexAPI, FlexTuple> flex, int target) {
            Map<Long, Integer> flexFiltered = filterAndTransform(flex);

            NPermuteAndCombiner<Long> g = new NPermuteAndCombiner<>();
            List<Set<Long>> splitted = split(flex);
            // Very costly operation if 'splitted' list is big.
            List<List<Long>> possibleSolutions = Lists.newArrayList(Sets
                    .cartesianProduct(splitted));
            // Add possibility for only 1 site participating.
            for (Collection<Long> key : splitted) {
                possibleSolutions.addAll(g.processSubsets(
                        Lists.newArrayList(key), 1));
            }

            Collection<Long> best = Lists.newArrayList();
            int score = 0;
            for (Collection<Long> poss : possibleSolutions) {
                int flexSum = 0;
                for (long l : poss) {
                    flexSum += flexFiltered.get(l);
                }
                if (diff(flexSum, target) < diff(score, target)) {
                    score = flexSum;
                    best = poss;
                }
            }
            context.dispatchActivation(flex, Sets.newLinkedHashSet(best));
        }

    },
    STATESEARCH() {

        @Override
        public void performAggregationStep(AggregationContext context, int t,
                LinkedListMultimap<SiteFlexAPI, FlexTuple> flex, int target) {
            // Graph<SiteFlexAPI, FlexTuple> space = new
            // DirectedOrderedSparseMultigraph<>();
            // filter maps for pos or neg.
            if (target > 0) {
                filter(flex, true);
            } else {
                filter(flex, false);
            }
            filterEmpty(flex);
            // Sort the maps
            LinkedListMultimap<SiteFlexAPI, FlexTuple> sorted = LinkedListMultimap
                    .create();
            for (SiteFlexAPI api : flex.keySet()) {
                List<FlexTuple> sortPlaceholder = Lists.newArrayList(flex
                        .get(api));
                Collections.sort(sortPlaceholder, new Comparator<FlexTuple>() {
                    @Override
                    public int compare(@Nullable FlexTuple o1,
                            @Nullable FlexTuple o2) {
                        checkNotNull(o1);
                        checkNotNull(o2);
                        return o1.getDeltaP() - o2.getDeltaP();
                    }
                });
                sorted.putAll(api, sortPlaceholder);
            }

            // Find the state space front that surpaces the target and filter
            // out everything above it.
            List<SiteFlexAPI> sites = Lists.newArrayList(sorted.keySet());
            int[] indexlistUpper = new int[sites.size()];
            for (int i = 0; i < indexlistUpper.length; i++) {
                indexlistUpper[i] = 0;
            }

            int sum = 0;
            boolean hasChanged = true;
            int absTarget = Math.abs(target);
            while (sum < absTarget && hasChanged) {
                hasChanged = false;
                sum = 0;
                for (int i = 0; i < indexlistUpper.length; i++) {
                    if (indexlistUpper[i] < sorted.get(sites.get(i)).size() - 1) {
                        indexlistUpper[i] += 1;
                        hasChanged = true;
                    }
                    sum += sorted.get(sites.get(i)).get(indexlistUpper[i])
                            .getDeltaP();
                }
            }
            // int[] indexlistLower = Arrays.copyOf(indexlistUpper,
            // indexlistUpper.length);

            // The filtering step.
            LinkedListMultimap<SiteFlexAPI, FlexTuple> capped = LinkedListMultimap
                    .create();
            for (int i = 0; i < indexlistUpper.length; i++) {
                capped.putAll(
                        sites.get(i),
                        sorted.get(sites.get(i)).subList(0,
                                indexlistUpper[i] + 1));
            }
            CARTESIANPRODUCT.performAggregationStep(context, t, capped, target);
            // FIXME: still a problem with profiles not corresponding to sites.
            // activation gets lost.
        }
    };

    @Override
    public abstract void performAggregationStep(AggregationContext context,
            int t, LinkedListMultimap<SiteFlexAPI, FlexTuple> flex, int target);

    /**
     * Returns a map from flexid to amount of power P that can be curtailed or
     * increased depending on the sign of t.
     * 
     * @param flex
     *            The set of flex tuples to start from.
     * @return the transformed map of id's to power P.
     */
    private static Map<Long, Integer> filterAndTransform(
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

    /**
     * Split the map of site to tuples into sets of ids per site.
     * 
     * @param flex
     *            the map to start from.
     * @return the splitted list of sets.
     */
    private static List<Set<Long>> split(
            LinkedListMultimap<SiteFlexAPI, FlexTuple> flex) {
        List<Set<Long>> res = Lists.newArrayList();
        Set<Long> tmp;
        for (SiteFlexAPI key : flex.keySet()) {
            tmp = Sets.newLinkedHashSet();
            for (FlexTuple f : flex.get(key)) {
                tmp.add(f.getId());
            }
            res.add(tmp);
        }
        return res;
    }

    private static int diff(int i, int target) {
        return Math.abs(target - i);
    }

    static void filter(LinkedListMultimap<SiteFlexAPI, FlexTuple> flex,
            boolean direction) {
        for (SiteFlexAPI api : flex.keySet()) {
            for (FlexTuple t : flex.get(api)) {
                if (t.getDirection() != direction) {
                    flex.remove(api, t);
                }
            }
        }
    }

    static void filterEmpty(LinkedListMultimap<SiteFlexAPI, FlexTuple> flex) {
        for (SiteFlexAPI api : flex.keySet()) {
            if (flex.get(api).isEmpty()) {
                flex.removeAll(api);
            }
        }
    }
}
