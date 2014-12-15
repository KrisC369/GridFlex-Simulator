package be.kuleuven.cs.gametheory.experimentation;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteSimulation;
import be.kuleuven.cs.gametheory.GameConfigurator;
import be.kuleuven.cs.gametheory.GameInstance;

/**
 * Configuration provider. This is a factory for generating participating
 * elements according to the implemented generator interfaces.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class GameConfiguratorEx implements GameConfigurator<Site, Aggregator> {
    private static final int CURRENT = 800;
    private static final int MIN = 500;
    private static final int MAX = 1000;
    private final MersenneTwister twister = new MersenneTwister(2412);

    @Override
    public Site getAgent() {
        return SiteSimulation.createDefault(CURRENT, MIN, MAX, 12);
    }

    @Override
    public GameInstance<Site, Aggregator> generateInstance() {
        return new TwoActionGameExample(twister.nextInt(), CURRENT);
    }

    @Override
    public int getActionSpaceSize() {
        return TwoActionGameExample.getActionspacesize();
    }

}
