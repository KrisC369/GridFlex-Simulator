package be.kuleuven.cs.gridflex.experimentation;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.gridflex.domain.site.Site;
import be.kuleuven.cs.gridflex.domain.site.SiteBuilder;

/**
 * Utility factory for creating site agents for simulation purposes.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class DefaultSiteAgentGenerator {

    private DefaultSiteAgentGenerator() {
    }

    /**
     * Creates a new sitesimulation agent with actual parameters chosen from
     * following parameters with guassian noise.
     *
     * @param twister
     *            The random generator to draw noise from.
     * @param max
     *            Maximum consumption level.
     * @param min
     *            Minimum consumption level.
     * @param starting
     *            Level to start from.
     * @param meanSteps
     *            The steps avegarge number of different levels between min and
     *            max.
     * @return A sitesimulation agent.
     */
    public static Site getAgent(final MersenneTwister twister, final int max, final int min,
            final int starting, final int meanSteps) {
        int current = (int) (twister.nextGaussian()
                * ((max - min) / (double) 4)) + starting;
        int steps = (int) Math.round(twister.nextGaussian() * 2 + meanSteps);
        if (current < min) {
            current = min;
        }
        if (current > max) {
            current = max;
        }
        if (steps <= 0) {
            steps = 1;
        }
        return SiteBuilder.newSiteSimulation().withBaseConsumption(current)
                .withMinConsumption(min).withMaxConsumption(max)
                .withTuples(steps).create();
    }
}
