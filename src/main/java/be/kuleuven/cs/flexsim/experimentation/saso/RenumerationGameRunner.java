package be.kuleuven.cs.flexsim.experimentation.saso;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.util.MathUtils;
import be.kuleuven.cs.flexsim.experimentation.GameConfiguratorEx;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentCallback;
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
public class RenumerationGameRunner {

    private static final int SEED = 3722;
    private MersenneTwister twister;
    private final int nAgents;
    private final int repititions;
    private final String loggerTag;
    private static final double DEF_STEPSIZE = 0.10;
    private final double stepSize;
    private final double factor;
    private final int availableProcs;
    private final Logger logger;
    private int counter;
    private int totalCombinations;

    protected RenumerationGameRunner(int repititions, int nAgents) {
        this(repititions, nAgents, "", DEF_STEPSIZE);
    }

    protected RenumerationGameRunner(int repititions, int nAgents,
            String loggerTag) {
        this(repititions, nAgents, loggerTag, DEF_STEPSIZE);
    }

    protected RenumerationGameRunner(int repititions, int nAgents,
            String loggerTag, double stepsize) {
        this.twister = new MersenneTwister(SEED);
        this.nAgents = nAgents;
        this.repititions = repititions;
        this.loggerTag = loggerTag;
        this.stepSize = stepsize;
        this.factor = 1.0 / stepsize;
        int rt = Runtime.getRuntime().availableProcessors() - 1;
        this.availableProcs = rt > 0 ? rt : 1;
        this.logger = LoggerFactory.getLogger(RenumerationGameRunner.class);
        this.counter = 0;
        this.totalCombinations = (int) MathUtils.multiCombinationSize(2,
                nAgents);
    }

    /**
     * Main start hook for these experimentations.
     */
    public final void execute() {
        for (int retributionFactor = 0; retributionFactor <= 1 * factor; retributionFactor += stepSize
                * factor) {
            double retrb = retributionFactor / factor;
            RenumerationGameConfigurator config = new RenumerationGameConfigurator(
                    retrb, twister);
            GameDirector director = new GameDirector(new Game<>(nAgents,
                    config, repititions));

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

    /**
     * Main start hook for these experimentations.
     */
    public final void executeBatch() {
        List<ExperimentAtom> metaExp = Lists.newArrayList();
        for (int retributionFactor = 0; retributionFactor <= 1 * factor; retributionFactor += stepSize
                * factor) {
            double retrb = retributionFactor / factor;
            GameConfiguratorEx ex = new GameConfiguratorEx(retrb / factor,
                    twister);
            GameDirector director = new GameDirector(new Game<>(nAgents, ex,
                    repititions));

            final List<ExperimentAtom> experiments = adapt(director);

            // batched
            final ExperimentAtom batch = new ExperimentAtomImpl() {
                @Override
                protected void execute() {
                    for (ExperimentAtom e : experiments) {
                        e.run();
                    }
                }

            };
            //

            final ResultWriter rw;
            if (loggerTag.isEmpty()) {
                rw = new GameResultWriter(director);
            } else {
                rw = new GameResultWriter(director, loggerTag);
            }
            rw.addResultComponent("RetributionFactor", String.valueOf(retrb));
            rw.addResultComponent("NumberOfAgents", String.valueOf(nAgents));
            rw.addResultComponent("Reps", String.valueOf(repititions));

            batch.registerCallbackOnFinish(new ExperimentCallback() {

                @Override
                public void callback(ExperimentAtom instance) {
                    rw.write();
                }
            });
            resetTwister();
            metaExp.add(batch);
        }
        LocalRunners.createCustomMultiThreadedRunner(availableProcs)
                .runExperiments(metaExp);

    }

    private List<ExperimentAtom> adapt(final GameDirector dir) {
        List<ExperimentAtom> experiments = Lists.newArrayList();
        for (final Playable p : dir.getPlayableVersions()) {
            this.counter++;
            final int current = counter;
            experiments.add(new ExperimentAtomImpl() {
                @Override
                protected void execute() {
                    try {
                        p.play();
                    } catch (final Exception e) {
                        logger.warn(
                                "Runtime exception caught while executing atom.",
                                e);
                        throw e;
                    }
                    dir.notifyVersionHasBeenPlayed(p);
                    printProgress(current);
                }
            });
        }
        return experiments;
    }

    private void resetTwister() {
        this.twister = new MersenneTwister(SEED);
    }

    private void printProgress(int progressCounter) {
        StringBuilder b = new StringBuilder();
        b.append("Simulating instance: ").append(progressCounter).append("/")
                .append(factor * repititions * totalCombinations);
        logger.warn(b.toString());
    }

    /**
     * Runs some experiments as a PoC.
     *
     * @param args
     *            commandline args.
     */
    public static void main(String[] args) {
        new RenumerationGameRunner(200, 3).execute();
    }
}
