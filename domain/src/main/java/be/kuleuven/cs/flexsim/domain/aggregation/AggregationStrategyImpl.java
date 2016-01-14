package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.NPermuteAndCombiner;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

/**
 * Some implementations of aggregation strategies.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public enum AggregationStrategyImpl implements AggregationStrategy {
    /**
     * An implementation that uses the cartesian product of sets to calculate
     * all possible solutions and then picks the best one. Costly,
     * performancewise. This is a brute force algorithm.
     */
    CARTESIANPRODUCT() {

        @Override
        public int performAggregationStep(AggregationContext context, int t,
                Multimap<SiteFlexAPI, FlexTuple> flex, int target) {
            AggregationUtils.filterEmpty(flex);
            Map<Long, Integer> flexFiltered = AggregationUtils
                    .filterAndTransform(flex);

            NPermuteAndCombiner<Long> g = new NPermuteAndCombiner<>();
            List<Set<Long>> splitted = AggregationUtils.split(flex);
            // Very costly operation if 'splitted' list is big.
            List<List<Long>> possibleSolutions = Lists
                    .newArrayList(Sets.cartesianProduct(splitted));
            // Add possibility for only 1 site participating.
            for (Collection<Long> key : splitted) {
                possibleSolutions
                        .addAll(g.processSubsets(Lists.newArrayList(key), 1));
            }

            Collection<Long> best = Lists.newArrayList();
            List<Collection<Long>> bestAlt = Lists.newArrayList();
            int score = 0;
            int with = 0;
            for (Collection<Long> poss : possibleSolutions) {
                int flexSum = 0;
                for (long l : poss) {
                    flexSum += flexFiltered.get(l);
                }
                if (diff(flexSum, target) < diff(score, target)) {
                    score = flexSum;
                    with = poss.size();
                    bestAlt = Lists.newArrayList();
                    bestAlt.add(poss);
                } else if (diff(flexSum, target) == diff(score, target)) {
                    if (poss.size() < with) {
                        with = poss.size();
                        bestAlt = Lists.newArrayList();
                        bestAlt.add(poss);
                    } else if (poss.size() == with) {
                        bestAlt.add(poss);
                    }
                }
            }
            if (!bestAlt.isEmpty()) {
                MersenneTwister r = new MersenneTwister(RANDOM_SEED);
                best = bestAlt.get(r.nextInt(bestAlt.size()));
            }
            if (!best.isEmpty()) {
                context.dispatchActivation(flex, Sets.newLinkedHashSet(best));
            }
            return score;
        }

    },

    /**
     * This strategy shifts the search horizon, specified by the minimal
     * combination of all sites providing useful flex and moves it just beyond
     * the target. All greater amounts of flexibility are removed from the set.
     * eventually the cartesianproduct version is called on the filtered
     * flex-set.
     */
    MOVINGHORIZON() {

        @Override
        public int performAggregationStep(AggregationContext context, int t,
                Multimap<SiteFlexAPI, FlexTuple> flex, int target) {

            // filter maps for pos or neg.
            if (target > 0) {
                AggregationUtils.filter(flex, true);
            } else {
                AggregationUtils.filter(flex, false);
            }
            AggregationUtils.filterEmpty(flex);
            // Sort the maps
            LinkedListMultimap<SiteFlexAPI, FlexTuple> sorted = AggregationUtils
                    .sort(flex);

            // Find the state space front that surpasses the target and filter
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
                    if (indexlistUpper[i] < sorted.get(sites.get(i)).size()
                            - 1) {
                        indexlistUpper[i] += 1;
                        hasChanged = true;
                    }
                    sum += sorted.get(sites.get(i)).get(indexlistUpper[i])
                            .getDeltaP();
                }
            }
            // The filtering step.
            LinkedListMultimap<SiteFlexAPI, FlexTuple> capped = LinkedListMultimap
                    .create();
            for (int i = 0; i < indexlistUpper.length; i++) {
                capped.putAll(sites.get(i), sorted.get(sites.get(i)).subList(0,
                        indexlistUpper[i] + 1));
            }
            return CARTESIANPRODUCT.performAggregationStep(context, t, capped,
                    target);
        }
    };

    private static final int RANDOM_SEED = 1423;

    @Override
    public abstract int performAggregationStep(AggregationContext context,
            int t, Multimap<SiteFlexAPI, FlexTuple> flex, int target);

    private static int diff(int i, int target) {
        return Math.abs(target - i);
    }
}
