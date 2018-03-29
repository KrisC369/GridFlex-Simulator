package be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data;

import be.kuleuven.cs.gridflex.domain.util.data.AbstractErrorDistribution;

/**
 * Random data generator using a Cauchy distribution.
 * Input distribution parameters should be defined for the [0,1] domain.
 * Outputs will be rescaled after draws to the [-1,1] domain.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MultiHorizonCauchyErrorGenerator extends MultiHorizonNormalErrorGenerator {
    /**
     * Defaut constructor.
     *
     * @param seed         The initial seed.
     * @param distribution The error distributions.
     */
    public MultiHorizonCauchyErrorGenerator(long seed,
            AbstractErrorDistribution distribution) {
        super(seed, distribution);
    }

    /**
     * Generate error value for given horizon.
     * Rescales the sample from [0,1] domain to [-1,1].
     *
     * @param i the horizon value.
     * @return The error value.
     */
    @Override
    public double generateErrorForHorizon(int i) {
        //Alternative way of calculating:
        //      [code]  CauchyDistribution c = new CauchyDistribution(); double sample = c.sample
        // (); [/code]
        //todo consider refactoring!
        double cauchyNrm =
                getTwisters().get(i).nextGaussian() / getTwisters().get(i).nextGaussian();
        double sample = cauchyNrm * getDistribution().getSdForHorizon(i) + getDistribution()
                .getMeanForHorizon(i);
        return (sample * 2) - 1;
    }
}
