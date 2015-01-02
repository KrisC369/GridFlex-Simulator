package be.kuleuven.cs.gametheory.experimentation;

import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.gametheory.Game;

/**
 * An example class running some experiments.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class GameRunner {

    private GameRunner() {
    }

    /**
     * Runs some experiments as a PoC.
     * 
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        GameConfiguratorEx ex = new GameConfiguratorEx();
        Game<Site, Aggregator> g = new Game<>(3, ex, 10);
        g.runExperiment();
        new GameResultWriter<>(g).write();
    }
}
