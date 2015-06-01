package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * Represents instances that allow curtailment of some sorts.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface CurtailableWorkstation extends Workstation {

    /**
     * Activate curtailment functionality.
     */
    void doFullCurtailment();

    /**
     * Restore the previous uncurtailed state.
     */
    void restore();

    /**
     * Returns whether this instance is in curtailment mode or not.
     * 
     * @return whether this instances is being curtailed.
     */
    boolean isCurtailed();

}
