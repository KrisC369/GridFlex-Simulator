package be.kuleuven.cs.flexsim.domain.energy.tso;

/**
 * Represents a participant in a mechanism where a contract is in place
 * guaranteeing compliance to a tso request for activation of flexibility.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface ContractualMechanismParticipant extends MechanismParticipant {
    /**
     * Signal to this participant the target amount.
     * 
     * @param timestep
     *            The time of this invocation.
     * 
     * @param target
     *            The amount to reach.
     */
    void signalTarget(int timestep, int target);
}
