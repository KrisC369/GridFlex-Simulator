package be.kuleuven.cs.flexsim.domain.site;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

import com.google.common.collect.Lists;

/**
 * Class representing a site module that makes abstraction of the underlying
 * mechanism and produces flex and consumption patterns.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class EquidistantSiteSimulation extends SiteSimulation {
    /**
     * Default constructor for equidistant flex site simulator.
     * 
     * @param base
     *            The base consumption to start from.
     * @param min
     *            The minimum limit for consumption.
     * @param max
     *            The maximum limit for consumption.
     * @param maxTuples
     *            The maximum tuples to generate per section of flex.
     */
    public EquidistantSiteSimulation(int base, int min, int max, int maxTuples) {
        super(base, min, max, maxTuples);
    }

    @Override
    protected void calculateCurrentFlex() {
        List<FlexTuple> upFlex = Lists.newArrayList();
        List<FlexTuple> downFlex = Lists.newArrayList();
        int bandwidth = (getMaxLimitConsumption() - getMinLimitConsumption())
                / getMaxTuples();
        for (int i = 1; i <= getMaxTuples(); i++) {
            int setpoint = getMinLimitConsumption() + i * bandwidth;
            int target = setpoint - getCurrentConsumption();
            upFlex.add(makeTuple(Math.abs(target), target > 0 ? true : false));
        }
        resetFlex(upFlex, downFlex);
    }

}
