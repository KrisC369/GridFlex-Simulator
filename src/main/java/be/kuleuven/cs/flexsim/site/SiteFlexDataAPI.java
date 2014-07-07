package be.kuleuven.cs.flexsim.site;

import java.util.List;

/**
 * API for gathering data about the flexibility for an entity.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public interface SiteFlexDataAPI {
    public List<FlexTuple> getFlexTuples();
}
