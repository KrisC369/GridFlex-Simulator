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
public final class RetributionFactorSensitivityRunner {

    private RetributionFactorSensitivityRunner() {
    }

    /**
     * Runs some experiments as a PoC.
     * 
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        for (double d = 1; d < 10; d++) {
            GameConfiguratorEx ex = new GameConfiguratorEx(d);
            Game<Site, Aggregator> g = new Game<>(3, ex, 10);
            g.runExperiment();
            new GameResultWriter<>(g).write();
        }
    }
}
