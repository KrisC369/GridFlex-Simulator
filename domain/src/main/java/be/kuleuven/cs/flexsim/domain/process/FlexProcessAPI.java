/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.process;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

/**
 * Represents a flexibility management api.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface FlexProcessAPI extends FlexControlAPI {
    /**
     * Query the process flexibility and get a list of flextuple profiles.
     * 
     * @return the flexibility tuples.
     */
    List<FlexTuple> getCurrentFlexbility();

}
