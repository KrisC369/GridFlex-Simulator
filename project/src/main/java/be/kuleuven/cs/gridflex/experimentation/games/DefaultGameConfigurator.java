package be.kuleuven.cs.gridflex.experimentation.games;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.gridflex.domain.aggregation.Aggregator;
import be.kuleuven.cs.gridflex.domain.site.Site;
import be.kuleuven.cs.gametheory.GameConfigurator;
import be.kuleuven.cs.gametheory.GameInstance;

/**
 * Configuration provider. This is a factory for generating participating
 * elements according to the implemented generator interfaces.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class DefaultGameConfigurator
        implements GameConfigurator<Site, Aggregator> {
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
    public DefaultGameConfigurator(final double factor) {
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
    public DefaultGameConfigurator(final double factor, final MersenneTwister twister) {
        this.retributionFactor = factor;
        this.twister = twister;
    }

    @Override
    public Site getAgent() {
        return DefaultSiteAgentGenerator.getAgent(twister, MAX, MIN, CURRENT,
                STEPS);
    }

    @Override
    public GameInstance<Site, Aggregator> generateInstance() {
        return new TwoActionGameExample(twister.nextInt(), CURRENT,
                retributionFactor);
    }

    @Override
    public int getActionSpaceSize() {
        return TwoActionGameExample.getActionspacesize();
    }

}
