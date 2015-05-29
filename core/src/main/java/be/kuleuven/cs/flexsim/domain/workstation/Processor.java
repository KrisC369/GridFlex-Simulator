package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.domain.resource.Resource;

/**
 * Represents an instance capable of handling the processing of resources.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
interface Processor {
    /**
     * Perform a processing step on a specified resource for a specified
     * duration of timesteps.
     * 
     * @param r
     *            The resource.
     * @param baseSteps
     *            The number of timesteps for performing the processing.
     */
    void doProcessingStep(Resource r, int baseSteps);
}
