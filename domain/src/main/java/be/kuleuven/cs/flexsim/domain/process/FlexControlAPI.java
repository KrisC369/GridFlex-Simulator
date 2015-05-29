package be.kuleuven.cs.flexsim.domain.process;

/**
 * Interface for controlling flexibility and executing flex profiles.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface FlexControlAPI {
    /**
     * Execute a curtailment profile in portfolio.
     * 
     * @param id
     *            the identifier of the profile.
     */
    void executeDownFlexProfile(long id);

    /**
     * Cancel curtailment or increase consumption according to a profile with a
     * specified id.
     * 
     * @param id
     *            the identifier of the profile;
     */
    void executeUpFlexProfile(long id);
}
