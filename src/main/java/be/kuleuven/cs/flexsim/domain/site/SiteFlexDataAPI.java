package be.kuleuven.cs.flexsim.domain.site;

import java.util.List;

/**
 * API for gathering data about the flexibility for an entity.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public interface SiteFlexDataAPI {
    /**
     * Queries this entity for its flexibility values.
     * 
     * @return a list of flexibility tuples.
     */
    public List<FlexTuple> getFlexTuples();
}
