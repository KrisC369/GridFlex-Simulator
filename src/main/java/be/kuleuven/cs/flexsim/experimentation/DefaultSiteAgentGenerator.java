package be.kuleuven.cs.flexsim.experimentation;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteSimulation;

/**
 * Utility factory for creating site agents for simulation purposes.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
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
    public static Site getAgent(MersenneTwister twister, int max, int min,
            int starting, int meanSteps) {
        int current = (int) (twister.nextGaussian() * ((max - min) / (double) 4))
                + starting;
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
        return SiteSimulation.createDefault(current, min, max, steps);
    }
}
