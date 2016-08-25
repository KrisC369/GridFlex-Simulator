package be.kuleuven.cs.flexsim.experimentation.saso;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.local.LocalRunners;
import be.kuleuven.cs.flexsim.experimentation.techreport.RetributionFactorSensitivityRunner;
import be.kuleuven.cs.flexsim.io.ResultWriter;
import be.kuleuven.cs.gametheory.Game;
import be.kuleuven.cs.gametheory.GameDirector;
import be.kuleuven.cs.gametheory.GameResult;
import be.kuleuven.cs.gametheory.GameResultWriter;
import be.kuleuven.cs.gametheory.Playable;
import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.List;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class RenumerationGameRunner extends RetributionFactorSensitivityRunner {

    private final List<GameResult> results;

    protected RenumerationGameRunner(final int repititions, final int nAgents) {
        this(repititions, nAgents, DEF_STEPSIZE);
    }

    protected RenumerationGameRunner(final int repititions, final int nAgents,
            final double stepsize) {
        super(repititions, nAgents, "RESULT" + nAgents + "A", stepsize);
        this.results = Lists.newArrayList();
    }

    /**
     * Main start hook for these experimentations.
     */
    @Override
    public final void execute() {
        for (int retributionFactor1 = 0; retributionFactor1 <= 1
                * factor; retributionFactor1 += stepSize * factor) {
            for (int retributionFactor2 = 0; retributionFactor2 <= 1
                    * factor; retributionFactor2 += stepSize * factor) {
                final double retrb1 = retributionFactor1 / factor;
                final double retrb2 = retributionFactor2 / factor;
                final RenumerationGameConfigurator config = new RenumerationGameConfigurator(
                        retrb1, retrb2, getTwister());
                final GameDirector director = new GameDirector(
                        new Game<>(nAgents, config, repititions));

                final List<ExperimentAtom> experiments = adapt(director);

                LocalRunners.createCustomMultiThreadedRunner(availableProcs)
                        .runExperiments(experiments);

                final ResultWriter rw;
                if (loggerTag.isEmpty()) {
                    rw = new GameResultWriter(director);
                } else {
                    rw = new GameResultWriter(director, loggerTag);
                }
                rw.addResultComponent("RetributionFactor1",
                        String.valueOf(retrb1));
                rw.addResultComponent("RetributionFactor2",
                        String.valueOf(retrb2));
                rw.addResultComponent("NumberOfAgents",
                        String.valueOf(nAgents));
                rw.addResultComponent("Reps", String.valueOf(repititions));
                rw.write();
                resetTwister();

                // create and store yaml.
                final GameResult result = director.getResults()
                        .withDescription("RetributionFactor1",
                                String.valueOf(retrb1))
                        .withDescription("RetributionFactor2",
                                String.valueOf(retrb2));
                this.results.add(result);

            }
        }
        dumpYamlResults();
    }

    private void dumpYamlResults() {
        final DumperOptions options = new DumperOptions();
        options.setExplicitStart(true);
        options.setAllowReadOnlyProperties(true);
        final Yaml yaml = new Yaml(options);
        final StringWriter writer = new StringWriter();
        yaml.dumpAll(results.iterator(), writer);
        LoggerFactory.getLogger("YAML").info(writer.toString());
    }

    private List<ExperimentAtom> adapt(final GameDirector dir) {
        final List<ExperimentAtom> experiments = Lists.newArrayList();
        for (final Playable p : dir.getPlayableVersions()) {
            this.incrementCounter();
            final int current = getCounter();
            experiments.add(new ExperimentAtomImpl() {
                @Override
                protected void execute() {
                    try {
                        p.play();
                    } catch (final RuntimeException e) {
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

    private void printProgress(final int progressCounter) {
        final StringBuilder b = new StringBuilder(25);
        b.append("Simulating instance: ").append(progressCounter).append("/")
                .append(factor * factor * repititions * totalCombinations);
        logger.warn(b.toString());
    }

    /**
     * Runs some experiments as a PoC.
     *
     * @param args commandline args.
     */
    public static void main(final String[] args) {
        if (args.length == 0) {
            new RenumerationGameRunner(1, 8).execute();
        } else if (args.length == 1) {
            try {
                final int agents = Integer.parseInt(args[0]);
                new RenumerationGameRunner(200, agents).execute();
            } catch (final RuntimeException e) {
                LoggerFactory.getLogger(RenumerationGameRunner.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        } else if (args.length == 2) {
            try {
                final int agents = Integer.parseInt(args[1]);
                final int reps = Integer.parseInt(args[0]);
                new RenumerationGameRunner(reps, agents).execute();
            } catch (final RuntimeException e) {
                LoggerFactory.getLogger(RenumerationGameRunner.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        }
    }
}
