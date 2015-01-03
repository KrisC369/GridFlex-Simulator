package be.kuleuven.cs.gametheory.experimentation;

import org.apache.commons.math3.random.MersenneTwister;

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

    private static final int SEED = 3722;
    private MersenneTwister twister;

    private RetributionFactorSensitivityRunner() {
        this.twister = new MersenneTwister(SEED);
    }

    /**
     * Run the experiments.
     */
    public void runExperiments() {
        for (double d = 1; d < 1500; d += 75) {
            GameConfiguratorEx ex = new GameConfiguratorEx(d, twister);
            Game<Site, Aggregator> g = new Game<>(3, ex, 20);
            g.runExperiment();
            ResultWriter rw = new GameResultWriter<>(g);
            rw.addResultComponent("RetributionFactor", String.valueOf(d));
            rw.write();
        }
    }

    /**
     * Runs some experiments as a PoC.
     * 
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        new RetributionFactorSensitivityRunner().runExperiments();
    }
}
