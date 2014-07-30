/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.process;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

/**
 * Represents a flexibility management api.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public interface FlexProcessAPI {
    /**
     * Query the process flexibility and get a list of flextuple profiles.
     * 
     * @return the flexibility tuples.
     */
    List<FlexTuple> getCurrentFlexbility();

    /**
     * Execute a curtailment profile in portfolio.
     * 
     * @param id
     *            the identifier of the profile.
     */
    void executeCurtailmentProfile(long id);

    /**
     * Cancel curtailment according to a profile with a specified id.
     * 
     * @param id
     *            the identifier of the profile;
     */
    void executeCancelCurtailmentProfile(long id);
}
