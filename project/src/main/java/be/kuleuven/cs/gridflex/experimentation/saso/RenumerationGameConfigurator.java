package be.kuleuven.cs.gridflex.experimentation.saso;

import be.kuleuven.cs.gridflex.domain.aggregation.brp.BRPAggregator;
import be.kuleuven.cs.gridflex.domain.site.Site;
import be.kuleuven.cs.gridflex.experimentation.DefaultSiteAgentGenerator;
import be.kuleuven.cs.gridflex.experimentation.TwoActionGameExample;
import be.kuleuven.cs.gametheory.GameConfigurator;
import be.kuleuven.cs.gametheory.GameInstance;
import org.apache.commons.math3.random.MersenneTwister;

/**
 * Configuration provider. This is a factory for generating participating
 * elements according to the implemented generator interfaces.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class RenumerationGameConfigurator
        implements GameConfigurator<Site, BRPAggregator> {
    private static final int SEED = 2412;
    private static final int CURRENT = 800;
    private static final int MIN = 500;
    private static final int MAX = 1000;
    private static final int STEPS = 8;
    private final MersenneTwister twister;
    private final double retributionFactor1;
    private final double retributionFactor2;

    /**
     * Constructor for these experiments.
     *
     * @param factor1
     *            The budget division factor for agg1.
     * @param factor2
     *            The budget division factor for agg2.
     */
    public RenumerationGameConfigurator(final double factor1, final double factor2) {
        this(factor1, factor2, new MersenneTwister(SEED));
    }

    /**
     * Constructor for these experiments.
     *
     * @param factor1
     *            The budget division factor for agg1.
     * @param factor2
     *            The budget division factor for agg2.
     * @param twister
     *            The random generator to use.
     */
    public RenumerationGameConfigurator(final double factor1, final double factor2,
            final MersenneTwister twister) {
        this.retributionFactor1 = factor1;
        this.retributionFactor2 = factor2;
        this.twister = twister;
    }

    @Override
    public Site getAgent() {
        return DefaultSiteAgentGenerator.getAgent(twister, MAX, MIN, CURRENT,
                STEPS);
    }

    @Override
    public GameInstance<Site, BRPAggregator> generateInstance() {
        return new RenumerationGame(twister.nextInt(), retributionFactor1,
                retributionFactor2);
    }

    @Override
    public int getActionSpaceSize() {
        return TwoActionGameExample.getActionspacesize();
    }

}
