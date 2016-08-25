package be.kuleuven.cs.flexsim.domain.energy.tso.contractual;

import be.kuleuven.cs.flexsim.domain.energy.tso.MechanismParticipant;
import be.kuleuven.cs.flexsim.domain.util.data.IntPowerCapabilityBand;

/**
 * Represents a participant in a mechanism where a contract is in place
 * guaranteeing compliance to a tso request for activation of flexibility.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface ContractualMechanismParticipant extends MechanismParticipant {
    /**
     * Signal to this participant the target amount.
     * 
     * @param timestep
     *            The time of this invocation.
     * @param target
     *            The amount to reach.
     */
    void signalTarget(int timestep, int target);

    /**
     * Returns the capabilities in terms of flexibility for this participant.
     * 
     * @return A power capability bandwith.
     */
    IntPowerCapabilityBand getPowerCapacity();

}
