package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
