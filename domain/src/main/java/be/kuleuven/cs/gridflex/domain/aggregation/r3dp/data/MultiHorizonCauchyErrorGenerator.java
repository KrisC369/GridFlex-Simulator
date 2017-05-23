package be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data;

import be.kuleuven.cs.gridflex.domain.util.data.AbstractErrorDistribution;

/**
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
     *
     * @param i the horizon value.
     * @return The error value.
     */
    @Override
    public double generateErrorForHorizon(int i) {
        //        CauchyDistribution c = new CauchyDistribution();
        //        double sample = c.sample();
        //todo use this!
        double cauchyNrm =
                getTwisters().get(i).nextGaussian() / getTwisters().get(i).nextGaussian();
        return cauchyNrm * getDistribution().getSdForHorizon(i) + getDistribution()
                .getMeanForHorizon(i);
    }
}
