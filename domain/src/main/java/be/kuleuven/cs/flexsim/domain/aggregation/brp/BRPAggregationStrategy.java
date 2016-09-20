/**
 *
 */
package be.kuleuven.cs.flexsim.domain.aggregation.brp;

import com.google.common.collect.Multimap;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregationContext;
import be.kuleuven.cs.flexsim.domain.aggregation.AggregationStrategy;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.FlexTuple;

/**
 * AggregationStrategy to use with BRP implementation of aggregator
 * functionality.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class BRPAggregationStrategy implements AggregationStrategy {

    /*
     * (non-Javadoc)
     * @see be.kuleuven.cs.flexsim.domain.aggregation.AggregationStrategy#
     * performAggregationStep
     * (be.kuleuven.cs.flexsim.domain.aggregation.AggregationContext, int,
     * com.google.common.collect.LinkedListMultimap, int)
     */
    @Override
    public int performAggregationStep(final AggregationContext context, final int t,
            final Multimap<SiteFlexAPI, FlexTuple> flex, final int target) {
        return 0;
    }
}
