package be.kuleuven.cs.flexsim.domain.process;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;

/**
 * represents a flexible process.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public interface FlexProcess extends SimulationComponent {
    /**
     * Query the process flexibility and get a list of flextuple profiles.
     * 
     * @return the flexibility tuples.
     */
    List<FlexTuple> getCurrentFlexbility();
}
