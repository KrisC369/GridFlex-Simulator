package be.kuleuven.cs.flexsim.domain.finances;

import java.util.Collection;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface ProcessTrackableSimulationComponent extends
        SimulationComponent {

    public abstract int getAggregatedTotalConsumptions();

    public abstract int getAggregatedLastStepConsumptions();

    public abstract List<Integer> getBufferOccupancyLevels();

    public abstract Collection<Resource> takeResources();

    public abstract void deliverResources(List<Resource> res);

}
