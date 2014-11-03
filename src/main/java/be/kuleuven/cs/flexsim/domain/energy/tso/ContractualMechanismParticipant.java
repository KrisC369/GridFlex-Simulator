package be.kuleuven.cs.flexsim.domain.energy.tso;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface ContractualMechanismParticipant extends MechanismParticipant {
    /**
     * Signal to this participant the target amount.
     * 
     * @param target
     *            The amount to reach.
     */
    void signalTarget(int target);
}
