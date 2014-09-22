package be.kuleuven.cs.flexsim.domain.aggregation;

import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

import com.google.common.collect.LinkedListMultimap;

/**
 * A strategy for performing aggregation duties.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface AggregationStrategy {
    /**
     * @param context
     * @param t
     * @param flex
     * @param target
     */
    void performAggregationStep(AggregationContext context, int t,
            LinkedListMultimap<SiteFlexAPI, FlexTuple> flex, int target);

}
