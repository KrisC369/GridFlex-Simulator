package be.kuleuven.cs.flexsim.domain.site;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

/**
 * API for gathering data about the flexibility for an entity.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public interface SiteFlexAPI {
    /**
     * Queries this entity for its flexibility values.
     * 
     * @return a list of flexibility tuples.
     */
    List<FlexTuple> getFlexTuples();

    /**
     * Schedule the activation of flexibility at an entity.
     * 
     * @param schedule
     *            list of controls requested
     */
    void activateFlex(ActivateFlexCommand schedule);
}
