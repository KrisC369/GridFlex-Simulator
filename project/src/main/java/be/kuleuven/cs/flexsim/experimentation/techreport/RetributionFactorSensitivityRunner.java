package be.kuleuven.cs.flexsim.experimentation.techreport;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;
import be.kuleuven.cs.flexsim.experimentation.DefaultGameConfigurator;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.local.LocalRunners;
import be.kuleuven.cs.flexsim.io.ResultWriter;
import be.kuleuven.cs.gametheory.Game;
import be.kuleuven.cs.gametheory.GameDirector;
import be.kuleuven.cs.gametheory.GameResultWriter;
import be.kuleuven.cs.gametheory.Playable;

import com.google.common.collect.Lists;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
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

    protected RetributionFactorSensitivityRunner(int repititions, int nAgents) {
        this(repititions, nAgents, "", DEF_STEPSIZE);
    }

    protected RetributionFactorSensitivityRunner(int repititions, int nAgents,
            String loggerTag) {
        this(repititions, nAgents, loggerTag, DEF_STEPSIZE);
    }

    protected RetributionFactorSensitivityRunner(int repititions, int nAgents,
            String loggerTag, double stepsize) {
        this.twister = new MersenneTwister(SEED);
        this.nAgents = nAgents;
        this.repititions = repititions;
        this.loggerTag = loggerTag;
        this.stepSize = stepsize;
        this.factor = 1.0 / stepsize;
        int rt = Runtime.getRuntime().availableProcessors() - 1;
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
        for (int retributionFactor = 0; retributionFactor <= 1 * factor; retributionFactor += stepSize
                * factor) {
            double retrb = retributionFactor / factor;
            DefaultGameConfigurator ex = new DefaultGameConfigurator(retrb,
                    twister);
            GameDirector director = new GameDirector(new Game<>(nAgents, ex,
                    repititions));

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

    private List<ExperimentAtom> adapt(final GameDirector dir) {
        List<ExperimentAtom> experiments = Lists.newArrayList();
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

    private void printProgress(int progressCounter) {
        StringBuilder b = new StringBuilder();
        b.append("Simulating instance: ").append(progressCounter).append("/")
                .append(factor * repititions * totalCombinations);
        logger.warn(b.toString());
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
     * @param counter
     *            the counter to set
     */
    protected final void incrementCounter() {
        this.counter = counter + 1;
    }

}
