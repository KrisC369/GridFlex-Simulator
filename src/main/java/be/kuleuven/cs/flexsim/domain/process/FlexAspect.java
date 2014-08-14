package be.kuleuven.cs.flexsim.domain.process;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
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
    List<FlexTuple> getFlexibility(
            List<? extends Workstation> curtailableStations,
            List<? extends Workstation> curtailedStations,
            LinkedListMultimap<Long, Workstation> profileMap);
}
