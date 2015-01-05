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
public final class RetributionFactorSensitivityRunner4A {

    private static final int SEED = 3722;
    private MersenneTwister twister;
    private final int nAgents = 4;
    private final int repititions = 15;

    private RetributionFactorSensitivityRunner4A() {
        this.twister = new MersenneTwister(SEED);
    }

    /**
     * Run the experiments.
     */
    public void runExperiments() {
        for (double d = 1; d < 500; d += 25) {
            GameConfiguratorEx ex = new GameConfiguratorEx(d, twister);
            Game<Site, Aggregator> g = new Game<>(nAgents, ex, repititions);
            g.runExperiment();
            ResultWriter rw = new GameResultWriter<>(g);
            rw.addResultComponent("RetributionFactor", String.valueOf(d));
            rw.addResultComponent("NumberOfAgents", String.valueOf(nAgents));
            rw.addResultComponent("Reps", String.valueOf(repititions));
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
        new RetributionFactorSensitivityRunner4A().runExperiments();
    }
}
