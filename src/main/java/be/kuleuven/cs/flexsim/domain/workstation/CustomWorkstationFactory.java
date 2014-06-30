package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;

/**
 * Factory template for creating custom workstations.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface CustomWorkstationFactory {
    /**
     * Create an instance of the station this factory is designed to build.
     * 
     * @param bufferIn
     *            Input buffer
     * @param bufferOut
     *            Output buffer
     * 
     * @return The station T.
     * 
     */
    Workstation createStation(Buffer<Resource> bufferIn,
            Buffer<Resource> bufferOut);
}
