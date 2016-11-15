package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import com.google.common.collect.Lists;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.List;
import java.util.Random;

/**
 * This class can generate normally distributed errors to forecasts.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class MultiHorizonErrorGenerator {

    private final ForecastHorizonErrorDistribution distribution;
    private final List<MersenneTwister> twisters;

    /**
     * Defaut constructor.
     *
     * @param seed         The initial seed.
     * @param distribution The error distributions.
     */
    public MultiHorizonErrorGenerator(long seed, ForecastHorizonErrorDistribution distribution) {
        this.distribution = distribution;
        twisters = Lists.newArrayList();
        for (int i = 0; i < distribution.getMaxForecastHorizon(); i++) {
            twisters.add(new MersenneTwister(seed + i));
        }
    }

    /**
     * Generate error value for given horizon.
     *
     * @param i the horizon value.
     * @return The error value.
     */
    public double generateErrorForHorizon(int i) {
        return twisters.get(i).nextGaussian() * distribution.getSdForHorizon(i) + distribution
                .getMeanForHorizon(i);
    }

}
