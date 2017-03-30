package be.kuleuven.cs.gridflex.domain.energy.consumption;

import be.kuleuven.cs.gridflex.simulation.SimulationComponent;

/**
 * This entity is capable of consuming energy.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface EnergyConsumptionTrackable extends SimulationComponent {

    /**
     * Returns the energy consumption amount during the last step.
     * 
     * @return an int representing consumption amount.
     */
    double getLastStepConsumption();

    /**
     * Returns the total energy consumption amount.
     * 
     * @return the total energy consumed.
     */
    double getTotalConsumption();

    /**
     * Returns the average energy consumption amount while processing the last
     * resource, approximately.
     *
     * @return The average energy consumption.
     */
    double getAverageConsumption();

}
