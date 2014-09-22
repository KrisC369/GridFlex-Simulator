/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Set;

import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

import com.google.common.collect.LinkedListMultimap;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
interface AggregationContext {
    void dispatchActivation(LinkedListMultimap<SiteFlexAPI, FlexTuple> flex,
            Set<Long> ids);
}
