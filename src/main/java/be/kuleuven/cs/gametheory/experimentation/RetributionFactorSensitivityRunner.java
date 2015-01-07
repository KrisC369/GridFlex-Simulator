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
public class RetributionFactorSensitivityRunner implements RunnableExperiment {

    private static final int SEED = 3722;
    private MersenneTwister twister;
    private final int nAgents;
    private final int repititions;

    protected RetributionFactorSensitivityRunner(int repititions, int nAgents) {
        this.twister = new MersenneTwister(SEED);
        this.nAgents = nAgents;
        this.repititions = repititions;
    }

    @Override
    public final void doExperimentRun(double retributionFactor) {
        GameConfiguratorEx ex = new GameConfiguratorEx(retributionFactor,
                twister);
        Game<Site, Aggregator> g = new Game<>(nAgents, ex, repititions);
        g.runExperiment();
        ResultWriter rw = new GameResultWriter<>(g);
        rw.addResultComponent("RetributionFactor",
                String.valueOf(retributionFactor));
        rw.addResultComponent("NumberOfAgents", String.valueOf(nAgents));
        rw.addResultComponent("Reps", String.valueOf(repititions));
        rw.write();
    }
}
