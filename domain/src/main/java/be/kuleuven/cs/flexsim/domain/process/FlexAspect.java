package be.kuleuven.cs.flexsim.domain.process;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.DualModeWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;

import com.google.common.collect.LinkedListMultimap;

/**
 * An aspect of flexibility. A certain measure or category of flexibility is
 * represented by this instance. Allows for the calculation of this aspect.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
interface FlexAspect {
    /**
     * Return the flexibility of this aspect.
     * 
     * @param curtailableStations
     *            The set of curtailable stations to work on.
     * @param curtailedStations
     *            The set of curtailed stations at this moment.
     * @param dualModeWorkstations
     *            The stations with two modi of operandi.
     * @param profileMap
     *            The reference to the map of id's to workstations.
     * @return the list of flexibility tuples for this aspect.
     */
    List<FlexTuple> getFlexibility(
            List<? extends Workstation> curtailableStations,
            List<? extends Workstation> curtailedStations,
            List<DualModeWorkstation> dualModeWorkstations,
            LinkedListMultimap<Long, Workstation> profileMap);
}
