package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingTSO;
import be.kuleuven.cs.flexsim.domain.energy.tso.ContractualMechanismParticipant;
import be.kuleuven.cs.flexsim.domain.energy.tso.PowerCapabilityBand;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * Subclasses the aggregator abstract class to add the behavior of reacting to
 * tso requests directly.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class ReactiveMechanismAggregator extends Aggregator implements
        ContractualMechanismParticipant, SimulationComponent {

    private BalancingTSO host;

    /**
     * Default constructor
     * 
     * @param host
     *            The host to register to.
     * @param strategy
     *            The strategy to adopt.
     * 
     */
    public ReactiveMechanismAggregator(BalancingTSO host,
            AggregationStrategy strategy) {
        super(strategy);
        this.host = host;
    }

    @Override
    public void signalTarget(int timestep, int target) {
        doAggregationStep(timestep, target);
    }

    @Override
    public void initialize(SimulationContext context) {

    }

    @Override
    public void afterTick(int t) {

    }

    @Override
    public void tick(int t) {
        signalCapacity();
    }

    private void signalCapacity() {
        int up = findMaxUpInPortfolio();
        int down = findMaxDownInPortfolio();
        host.signalNewLimits(this, PowerCapabilityBand.create(down, up));
    }

    private int findMaxUpInPortfolio() {
        return 0;
    }

    private int findMaxDownInPortfolio() {
        return 0;
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }
}
