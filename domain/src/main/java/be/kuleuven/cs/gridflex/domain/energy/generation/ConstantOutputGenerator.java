package be.kuleuven.cs.gridflex.domain.energy.generation;

import be.kuleuven.cs.gridflex.simulation.SimulationContext;

/**
 * Electricity generator capable of producing a constant output.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
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
    public ConstantOutputGenerator(final int base) {
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
    public void afterTick(final int t) {
        this.total += this.output;
    }

    @Override
    public void tick(final int t) {
    }

    @Override
    public void initialize(final SimulationContext context) {
    }

}
