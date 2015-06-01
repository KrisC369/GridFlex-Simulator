package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Set;

import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

import com.google.common.collect.Multimap;

/**
 * A context for the aggregation strategy. This represents an entity capable of
 * dispatching the final activation requests.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface AggregationContext {

    /**
     * Dispatch an activation signal.
     *
     * @param flex
     *            The flex mapping.
     * @param ids
     *            the ids that reference the profile to activate.
     */
    void dispatchActivation(Multimap<SiteFlexAPI, FlexTuple> flex, Set<Long> ids);
}
