package be.kuleuven.cs.flexsim.experimentation.saso;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.flexsim.domain.aggregation.brp.BRPAggregator;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteSimulation;
import be.kuleuven.cs.flexsim.experimentation.TwoActionGameExample;
import be.kuleuven.cs.gametheory.GameConfigurator;
import be.kuleuven.cs.gametheory.GameInstance;

/**
 * Configuration provider. This is a factory for generating participating
 * elements according to the implemented generator interfaces.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class RenumerationGameConfigurator implements
        GameConfigurator<Site, BRPAggregator> {
    private static final int CURRENT = 800;
    private static final int MIN = 500;
    private static final int MAX = 1000;
    private static final int STEPS = 8;
    private final MersenneTwister twister;
    private final double retributionFactor;

    /**
     * Constructor for these experiments.
     *
     * @param factor
     *            the retribution factor.
     */
    public RenumerationGameConfigurator(double factor) {
        this(factor, new MersenneTwister(2412));
    }

    /**
     * Constructor for these experiments.
     *
     * @param factor
     *            the retribution factor.
     * @param twister
     *            The random generator to use.
     */
    public RenumerationGameConfigurator(double factor, MersenneTwister twister) {
        this.retributionFactor = factor;
        this.twister = twister;
    }

    @Override
    public Site getAgent() {
        int current = (int) (twister.nextGaussian() * ((MAX - MIN) / 4))
                + CURRENT;
        int steps = (int) Math.round(twister.nextGaussian() * 2 + STEPS);
        if (current < MIN) {
            current = MIN;
        }
        if (current > MAX) {
            current = MAX;
        }
        if (steps <= 0) {
            steps = 1;
        }
        return SiteSimulation.createDefault(current, MIN, MAX, steps);
    }

    @Override
    public GameInstance<Site, BRPAggregator> generateInstance() {
        return new RenumerationGame(twister.nextInt(), CURRENT,
                retributionFactor);
    }

    @Override
    public int getActionSpaceSize() {
        return TwoActionGameExample.getActionspacesize();
    }

}
