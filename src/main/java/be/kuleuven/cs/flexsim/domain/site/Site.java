package be.kuleuven.cs.flexsim.domain.site;

import be.kuleuven.cs.flexsim.domain.process.ProductionLine;

/**
 * Represents a production site with possibly multiple production lines.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public interface Site extends SiteFlexControlAPI, SiteFlexDataAPI {

    /**
     * Tests if this site has the specified production line.
     * 
     * @param line
     *            the line to test.
     * @return True if this site contains the line.
     */
    boolean containsLine(ProductionLine line);

}
