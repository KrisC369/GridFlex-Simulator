/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.process;

import be.kuleuven.cs.flexsim.domain.util.FlexTuple;

import java.util.List;

/**
 * Represents a flexibility management api.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface FlexProcessAPI extends FlexControlAPI {
    /**
     * Query the process flexibility and get a list of flextuple profiles.
     * 
     * @return the flexibility tuples.
     */
    List<FlexTuple> getCurrentFlexbility();

}
