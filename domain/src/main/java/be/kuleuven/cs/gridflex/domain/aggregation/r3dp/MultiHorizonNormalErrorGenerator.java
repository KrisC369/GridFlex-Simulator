package be.kuleuven.cs.gridflex.domain.aggregation.r3dp;

import be.kuleuven.cs.gridflex.domain.util.data.AbstractErrorDistribution;

/**
 * This class can generate normally distributed errors to forecasts.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MultiHorizonNormalErrorGenerator extends MultiHorizonErrorGenerator {

    /**
     * Defaut constructor.
     *
     * @param seed         The initial seed.
     * @param distribution The error distributions.
     */
    public MultiHorizonNormalErrorGenerator(long seed,
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
        return getTwisters().get(i).nextGaussian() * getDistribution().getSdForHorizon(i)
                + getDistribution()
                .getMeanForHorizon(i);
    }
}
