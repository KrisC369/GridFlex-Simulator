package be.kuleuven.cs.flexsim.domain.aggregation;

import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.FlexTuple;
import com.google.common.collect.Multimap;

import java.util.Set;

/**
 * A context for the aggregation strategy. This represents an entity capable of
 * dispatching the final activation requests.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@FunctionalInterface
public interface AggregationContext {

    /**
     * Dispatch an activation signal.
     *
     * @param flex The flex mapping.
     * @param ids  the ids that reference the profile to activate.
     */
    void dispatchActivation(Multimap<SiteFlexAPI, FlexTuple> flex,
            Set<Long> ids);
}
