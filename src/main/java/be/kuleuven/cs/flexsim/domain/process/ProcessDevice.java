/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.process;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.domain.workstation.TradeofSteerableWorkstation;

/**
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

    void executeCancelCurtailment(long id,
            List<CurtailableWorkstation> curtailableStations);

    void executeCurtailment(long id, List<CurtailableWorkstation> list);

    void invalidate();

    List<FlexTuple> getCurrentFlexbility(
            List<CurtailableWorkstation> curtailableWorkstations,
            List<TradeofSteerableWorkstation> tradeofSteerableWorkstations);
}
