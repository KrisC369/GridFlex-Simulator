package be.kuleuven.cs.flexsim.domain.energy.generation;

import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * Electricity generator capable of producing a constant output.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class ConstantOutputGenerator implements EnergyProductionTrackable {

    private final int output;
    private long total;

    /**
     * Default constructor.
     * 
     * @param base
     *            The base output level for this generator.
     */
    public ConstantOutputGenerator(int base) {
        this.output = base;
        this.total = 0;
    }

    /**
     * Default getter for the output.
     * 
     * @return the output
     */
    public final int getOutput() {
        return output;
    }

    @Override
    public double getLastStepProduction() {
        return output;
    }

    @Override
    public double getTotalProduction() {
        return this.total;
    }

    @Override
    public void afterTick(int t) {
        this.total += this.output;
    }

    @Override
    public void tick(int t) {
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    @Override
    public void initialize(SimulationContext context) {
    }

}
