package be.kuleuven.cs.flexsim.experimentation.techreport;

import be.kuleuven.cs.flexsim.domain.aggregation.brp.BRPAggregator;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.util.MathUtils;
import be.kuleuven.cs.flexsim.experimentation.DefaultGameConfigurator;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.local.LocalRunners;
import be.kuleuven.cs.flexsim.io.ResultWriter;
import be.kuleuven.cs.gametheory.results.GameResultWriter;
import be.kuleuven.cs.gametheory.Playable;
import be.kuleuven.cs.gametheory.standalone.Game;
import be.kuleuven.cs.gametheory.standalone.GameDirector;
import com.google.common.collect.Lists;
import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class RetributionFactorSensitivityRunner {

    protected static final int SEED = 3722;
    private MersenneTwister twister;
    protected final int nAgents;
    protected final int repititions;
    protected final String loggerTag;
    protected static final double DEF_STEPSIZE = 0.10;
    protected final double stepSize;
    protected final double factor;
    protected final int availableProcs;
    protected final Logger logger;
    private int counter;
    protected final int totalCombinations;

    protected RetributionFactorSensitivityRunner(final int repititions, final int nAgents) {
        this(repititions, nAgents, "", DEF_STEPSIZE);
    }

    protected RetributionFactorSensitivityRunner(final int repititions, final int nAgents,
            final String loggerTag) {
        this(repititions, nAgents, loggerTag, DEF_STEPSIZE);
    }

    protected RetributionFactorSensitivityRunner(final int repititions, final int nAgents,
            final String loggerTag, final double stepsize) {
        this.twister = new MersenneTwister(SEED);
        this.nAgents = nAgents;
        this.repititions = repititions;
        this.loggerTag = loggerTag;
        this.stepSize = stepsize;
        this.factor = 1.0 / stepsize;
        final int rt = Runtime.getRuntime().availableProcessors() - 1;
        this.availableProcs = rt > 0 ? rt : 1;
        this.logger = LoggerFactory
                .getLogger(RetributionFactorSensitivityRunner.class);
        this.counter = 0;
        this.totalCombinations = (int) MathUtils.multiCombinationSize(2,
                nAgents);
    }

    /**
     * Main start hook for these experimentations.
     */
    public void execute() {
        for (int retributionFactor = 0; retributionFactor <= 1
                * factor; retributionFactor += stepSize * factor) {
            final double retrb = retributionFactor / factor;
            final DefaultGameConfigurator ex = new DefaultGameConfigurator(retrb,
                    twister);
            final GameDirector director = new GameDirector(
                    new Game<>(nAgents, ex, repititions));

            final List<ExperimentAtom> experiments = adapt(director);

            LocalRunners.createCustomMultiThreadedRunner(availableProcs)
                    .runExperiments(experiments);

            final ResultWriter rw;
            if (loggerTag.isEmpty()) {
                rw = new GameResultWriter(director);
            } else {
                rw = new GameResultWriter(director, loggerTag);
            }
            rw.addResultComponent("RetributionFactor", String.valueOf(retrb));
            rw.addResultComponent("NumberOfAgents", String.valueOf(nAgents));
            rw.addResultComponent("Reps", String.valueOf(repititions));
            rw.write();
            resetTwister();
        }
    }

    private List<ExperimentAtom> adapt(final GameDirector<Site, BRPAggregator> dir) {
        final List<ExperimentAtom> experiments = Lists.newArrayList();
        for (final Playable p : dir.getPlayableVersions()) {
            this.counter++;
            final int current = counter;
            experiments.add(new ExperimentAtomImpl() {
                @Override
                protected void execute() {
                    p.play();
                    dir.notifyVersionHasBeenPlayed(p);
                    printProgress(current);
                }
            });
        }
        return experiments;
    }

    protected final void resetTwister() {
        this.twister = new MersenneTwister(SEED);
    }

    private void printProgress(final int progressCounter) {
        final String b = "Simulating instance: " + progressCounter + "/" +
                factor * repititions * totalCombinations;
        logger.warn(b);
    }

    /**
     * @return the twister
     */
    protected final MersenneTwister getTwister() {
        return twister;
    }

    /**
     * @return the counter
     */
    protected final int getCounter() {
        return counter;
    }

    /**
     * @param counter the counter to set
     */
    protected final void incrementCounter() {
        this.counter = counter + 1;
    }

}
