package be.kuleuven.cs.flexsim.domain.aggregation;

import be.kuleuven.cs.flexsim.domain.energy.tso.ContractualMechanismParticipant;

/**
 * Subclasses the aggregator abstract class to add the behavior of reacting to
 * tso requests directly.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class ReactiveMechanismAggregator extends Aggregator implements
        ContractualMechanismParticipant {

    /**
     * Default constructor
     * 
     */
    public ReactiveMechanismAggregator() {
        super();
    }

    @Override
    public void signalTarget(int timestep, int target) {
        doAggregationStep(timestep, target);
    }

}
