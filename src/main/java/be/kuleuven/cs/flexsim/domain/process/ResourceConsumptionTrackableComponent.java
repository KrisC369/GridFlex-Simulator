package be.kuleuven.cs.flexsim.domain.process;

import java.util.Collection;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;

/**
 * Represents a simulation component for which the process can be tracked and
 * steered in terms of input-output of resources.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
public interface ResourceConsumptionTrackableComponent extends
        SimulationComponent {

    /**
     * Returns the total energy consumption aggregated over all subcomponents.
     * 
     * @return the total aggregated energy consumed.
     */
    int getAggregatedTotalConsumptions();

    /**
     * Returns the energy consumption in the last step aggregated over all
     * subcomponents.
     * 
     * @return the aggregated energy consumed in the last simulation step.
     */
    int getAggregatedLastStepConsumptions();

    /**
     * Get an list of the buffer levels ordered by the location of the buffer in
     * the component.
     * 
     * @return a list of occupancy levels.
     */
    List<Integer> getBufferOccupancyLevels();

    /**
     * Take the finished resources from the end of the process.
     * 
     * @return the finished resources.
     */
    Collection<Resource> takeResources();

    /**
     * Deliver unprocessed resources to the primary buffer at the beginning of
     * the process.
     * 
     * @param res
     *            the resources that serve as input
     */
    void deliverResources(List<Resource> res);

}
