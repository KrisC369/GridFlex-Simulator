package be.kuleuven.cs.flexsim.domain.site;

import be.kuleuven.cs.flexsim.domain.util.FlexTuple;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;

import java.util.List;

/**
 * API for gathering data about the flexibility for an entity.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
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

    /**
     * Adds a listener to activation commands.
     *
     * @param listener
     *            the listener to attach.
     */
    void addActivationListener(Listener<? super FlexTuple> listener);
}
