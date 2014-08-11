package be.kuleuven.cs.flexsim.domain.site;

import be.kuleuven.cs.flexsim.domain.process.FlexProcess;
import be.kuleuven.cs.flexsim.domain.process.ResourceConsumptionTrackableComponent;

/**
 * Represents a production site with possibly multiple production lines.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface Site extends SiteFlexAPI,
        ResourceConsumptionTrackableComponent {

    /**
     * Tests if this site has the specified process.
     * 
     * @param process
     *            the process to test.
     * @return True if this site contains the process.
     */
    boolean containsLine(FlexProcess process);

}
