package be.kuleuven.cs.flexsim.domain.aggregation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Utility class provind some static utility functions to be used by aggregation
 * engines.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class AggregationUtils {
    private AggregationUtils() {
    }

    /**
     * Returns a map from flexid to amount of power P that can be curtailed or
     * increased depending on the sign of t.
     *
     * @param flex
     *            The set of flex tuples to start from as a multimap from
     *            SiteFlexAPI objects to FlexTuples.
     * @return the transformed map of id's to power P.
     */
    public static Map<Long, Integer> filterAndTransform(
            Multimap<SiteFlexAPI, FlexTuple> flex) {
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
     * Split the map of sites to tuples into sets of ids per site.
     *
     * @param flex
     *            the map to start from.
     * @return the splitted list of sets.
     */
    public static List<Set<Long>> split(Multimap<SiteFlexAPI, FlexTuple> flex) {
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

    /**
     * Filter flex in the opposing direction of the target out from the input
     * map.
     *
     * @param flex
     *            the input map.
     * @param direction
     *            the direction to filter in.
     */
    public static void filter(Multimap<SiteFlexAPI, FlexTuple> flex,
            boolean direction) {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> copy = LinkedListMultimap
                .create(flex);
        for (SiteFlexAPI api : copy.keySet()) {
            for (FlexTuple t : copy.get(api)) {
                if (t.getDirection() != direction) {
                    flex.remove(api, t);
                }
            }
        }
    }

    /**
     * Remove the empty sets from the input flex map.
     *
     * @param flex
     *            the input map.
     */
    public static void filterEmpty(Multimap<SiteFlexAPI, FlexTuple> flex) {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> copy = LinkedListMultimap
                .create(flex);
        for (SiteFlexAPI api : copy.keySet()) {
            if (copy.get(api).isEmpty()) {
                flex.removeAll(api);
            } else {
                for (FlexTuple t : copy.get(api)) {
                    if (t.getDeltaP() == 0) {
                        flex.get(api).remove(t);
                    }
                }
            }
        }
    }

    /**
     * Sorts the input flex profiles according to delta-P values in ascending
     * order.
     *
     * @param flex
     *            The input.
     * @return the sorted flex map.
     */
    public static LinkedListMultimap<SiteFlexAPI, FlexTuple> sort(
            Multimap<SiteFlexAPI, FlexTuple> flex) {
        LinkedListMultimap<SiteFlexAPI, FlexTuple> sorted = LinkedListMultimap
                .create();
        for (SiteFlexAPI api : flex.keySet()) {
            List<FlexTuple> sortPlaceholder = Lists.newArrayList(flex.get(api));
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
        return sorted;
    };
}
