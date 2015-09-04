package be.kuleuven.cs.flexsim.domain.energy.generation;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;

/**
 * This entity is capable of producing energy.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface EnergyProductionTrackable extends SimulationComponent {
    /**
     * Returns the energy production amount during the last step.
     * 
     * @return a double representing production amount.
     */
    double getLastStepProduction();

    /**
     * Returns the total energy production amount.
     * 
     * @return the total energy Produced.
     */
    double getTotalProduction();

}
