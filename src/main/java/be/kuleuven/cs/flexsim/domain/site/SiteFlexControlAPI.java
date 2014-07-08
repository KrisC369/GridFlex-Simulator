package be.kuleuven.cs.flexsim.domain.site;


/**
 * Api for sending control signals to acitvate flexibility at an entity.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public interface SiteFlexControlAPI {

    /**
     * Schedule the activation of flexibility at an entity.
     * 
     * @param schedule
     *            list of controls requested
     */
    void activateFlex(ActivateFlexCommand schedule);
}
