/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.process;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.DualModeWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.TradeofSteerableWorkstation;

/**
 * Represents a Control device in charge of managing flexibility of a single
 * process.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
interface ProcessDevice {
    /**
     * Add an aspect of flexibility to this process Device. The PD will now take
     * into account this aspect while calculating flex.
     * 
     * @param aspect
     *            The aspect to add to this device.
     * @return the process device with this aspect added.
     */
    ProcessDevice addFlexAspect(FlexAspect aspect);

    /**
     * Execute this cancel curtailment request.
     * 
     * @param id
     *            the id of the request.
     * @param stations
     *            The stations to work with.
     */
    void executeCancelCurtailment(long id, List<CurtailableWorkstation> stations);

    /**
     * Execute this curtailment request.
     * 
     * @param id
     *            the id of the request.
     * @param list
     *            the List of curtailable stations to work with.
     */
    void executeCurtailment(long id, List<CurtailableWorkstation> list);

    /**
     * Invalidate the current cache of profiles.
     */
    void invalidate();

    /**
     * Returns the current measure of flexibility.
     * 
     * @param curtailableWorkstations
     *            The stations that can curtail.
     * @param tradeofSteerableWorkstations
     *            The stations that can steer their output.
     * @param dualModeStations
     *            The stations that have two distinct modes of consumption.
     * @return the list of flexibility tuples for this process.
     */
    List<FlexTuple> getCurrentFlexbility(
            List<CurtailableWorkstation> curtailableWorkstations,
            List<TradeofSteerableWorkstation> tradeofSteerableWorkstations,
            List<DualModeWorkstation> dualModeStations);
}
