package be.kuleuven.cs.gridflex.domain.aggregation.r3dp;

import be.kuleuven.cs.gridflex.domain.util.data.AbstractErrorDistribution;
import com.google.common.collect.Lists;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class MultiHorizonErrorGenerator {
    private final AbstractErrorDistribution distribution;
    private final List<MersenneTwister> twisters;

    /**
     * Defaut constructor.
     *
     * @param seed         The initial seed.
     * @param distribution The error distributions.
     */
    public MultiHorizonErrorGenerator(
            long seed, AbstractErrorDistribution distribution) {
        twisters = Lists.newArrayList();
        this.distribution = distribution;
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
    public abstract double generateErrorForHorizon(int i);

    protected final AbstractErrorDistribution getDistribution() {
        return distribution;
    }

    protected final List<MersenneTwister> getTwisters() {
        return twisters;
    }
}
