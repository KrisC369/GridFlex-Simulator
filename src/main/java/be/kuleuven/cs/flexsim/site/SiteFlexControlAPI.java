package be.kuleuven.cs.flexsim.site;

import java.util.List;

/**
 * Api for sending control signals to acitvate flexibility at an entity.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public interface SiteFlexControlAPI {
    /**
     * Test whether the process device configuration is up-to-date
     * 
     * @return true if this config is still up to date.
     */
    boolean checkConfig();

    /**
     * Schedule the process device for particular controls, like an activation
     * 
     * @param schedule
     *            list of controls requested
     */
    void setControlSchedules(List<ControlSchedule> schedule);
}
