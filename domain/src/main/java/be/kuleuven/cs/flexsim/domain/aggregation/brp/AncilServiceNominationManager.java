package be.kuleuven.cs.flexsim.domain.aggregation.brp;

/**
 * Manages ancilleray services nomination notifications. These includes
 * notifications of the amount of flex is being activated to remedy imbalances.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface AncilServiceNominationManager {

    /**
     * Register a nomination with this nominationManager.
     * 
     * @param target
     *            The nomination to register.
     */
    void registerNomination(Nomination target);
}
