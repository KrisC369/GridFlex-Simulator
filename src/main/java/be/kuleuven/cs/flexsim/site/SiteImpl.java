package be.kuleuven.cs.flexsim.site;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import be.kuleuven.cs.flexsim.domain.factory.ProductionLine;

import com.google.common.collect.Lists;

/**
 * An implementation for the Site interface
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public class SiteImpl implements Site {

    private List<ProductionLine> processes;

    /**
     * Default constructor based on lines.
     * 
     * @param lines
     *            The lines present in this site.
     */
    public SiteImpl(ProductionLine... lines) {
        processes = Lists.newArrayList(lines);
    }

    @Override
    public List<FlexTuple> getFlexTuples() {
        return Collections.emptyList();
    }

    @Override
    public void activateFlex(ActivateFlexCommand schedule) {
        throw new NotImplementedException("Not implemented yet!");
    }

    @Override
    public boolean containsLine(ProductionLine line1) {
        return getProcesses().contains(line1);
    }

    /**
     * @return the processes
     */
    final List<ProductionLine> getProcesses() {
        return processes;
    }

}
