package be.kuleuven.cs.gametheory.experimentation;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteSimulation;
import be.kuleuven.cs.gametheory.AgentGenerator;
import be.kuleuven.cs.gametheory.GameInstance;
import be.kuleuven.cs.gametheory.GameInstanceGenerator;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class GameConfiguratorEx implements AgentGenerator<Site>,
        GameInstanceGenerator<Site, Aggregator> {
    private final int current = 800, min = 500, max = 1000;
    private MersenneTwister twister = new MersenneTwister(2412);

    @Override
    public Site getAgent() {
        return SiteSimulation.createDefault(current, min, max, 12);
    }

    @Override
    public GameInstance<Site, Aggregator> generateInstance() {
        return new ExpGameInstance(twister.nextInt(), 2, 2);
    }

}
