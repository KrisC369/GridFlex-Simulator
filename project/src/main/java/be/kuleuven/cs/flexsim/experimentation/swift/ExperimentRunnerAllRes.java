package be.kuleuven.cs.flexsim.experimentation.swift;

import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.CompetitiveCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.CooperativeCongestionSolver;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.AbstractTimeSeriesImplementation;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.runners.local.LocalRunners;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Experiment runner that gathers and produces all result metrics that are of
 * interest.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunnerAllRes implements ExecutableExperiment {

    private static final String RESULT_CONSOLE_LOGGER = "CONSOLERESULT";
    private static final long SEED = 1312421L;
    private static final boolean RUN_MULTI_THREADED = true;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final int DEFAULT_N_REPITITIONS = 1000;
    private static final int ALLOWED_EXCESS = 33;
    private static final boolean ALLOW_LESS_ACTIVATIONS = true;
    private static final double TOTAL_PRODUCED_E = 36360905;
    private static final String CL_ERROR = "Unparseable cl parameters passed";
    private static final String FILE = "be/kuleuven/cs/flexsim/experimentation/data"
            + "/2kwartOpEnNeer.csv";
    private static final String COLUMN = "verlies aan energie";
    private final int n;
    private final int nagents;
    private final int allowedExcess;
    private final List<Double> mainRes1 = Lists.newCopyOnWriteArrayList();
    private final List<Double> mainRes2 = Lists.newCopyOnWriteArrayList();
    private final List<Double> actRes1 = Lists.newCopyOnWriteArrayList();
    private final List<Double> actRes2 = Lists.newCopyOnWriteArrayList();
    private final List<Double> solvRes1 = Lists.newCopyOnWriteArrayList();
    private final List<Double> solvRes2 = Lists.newCopyOnWriteArrayList();
    private final List<Double> actEffRes1 = Lists.newCopyOnWriteArrayList();
    private final List<Double> actEffRes2 = Lists.newCopyOnWriteArrayList();
    private final List<Double> remediedCong1 = Lists.newCopyOnWriteArrayList();
    private final List<Double> remediedCong2 = Lists.newCopyOnWriteArrayList();
    private boolean competitive = true;

    protected ExperimentRunnerAllRes(final int n, final int nagents, final int allowed) {
        this.n = n;
        this.nagents = nagents;
        this.allowedExcess = allowed;
    }

    /**
     * main method used to run the application.
     *
     * @param args StdIn args.
     */
    public static void main(final String[] args) {
        final ExpGenerator gen = (reps, agents,
                allowed) -> new ExperimentRunnerAllRes(reps, agents, allowed);
        parseInput(gen, args, DEFAULT_N_REPITITIONS, ALLOWED_EXCESS);
    }

    protected static void parseInput(final ExpGenerator gen, final String[] args, final int n,
            final int allowedEx) {

        if (args.length == 0) {
            startExperiment(gen, 10, 81, allowedEx);
        } else if (args.length == 1) {
            try {
                final int agents = Integer.parseInt(args[0]);
                startExperiment(gen, n, agents, allowedEx);
            } catch (final RuntimeException e) {
                LoggerFactory.getLogger(ExperimentRunnerAllRes.class)
                        .error(CL_ERROR);
                throw e;
            }
        } else if (args.length == 2) {
            try {
                final int agents = Integer.parseInt(args[1]);
                final int reps = Integer.parseInt(args[0]);
                startExperiment(gen, reps, agents, allowedEx);
            } catch (final RuntimeException e) {
                LoggerFactory.getLogger(ExperimentRunnerAllRes.class)
                        .error(CL_ERROR);
                throw e;
            }
        } else if (args.length == 3) {
            try {
                final int agents = Integer.parseInt(args[1]);
                final int reps = Integer.parseInt(args[0]);
                final int allowed = Integer.parseInt(args[2]);
                startExperiment(gen, reps, agents, allowed);
            } catch (final RuntimeException e) {
                LoggerFactory.getLogger(ExperimentRunnerAllRes.class)
                        .error(CL_ERROR);
                throw e;
            }
        }

    }

    static void startExperiment(final ExpGenerator gen, final int reps, final int agents,
            final int allowed) {
        gen.getExperiment(reps, agents, allowed).execute();
    }

    /**
     * Execute experiments.
     */
    @Override
    public void execute() {
        runBatch();
        competitive = false;
        runBatch();
        logResults();
    }

    @SuppressWarnings("unused")
    private static void generateRates(final int n) {
        final GammaDistribution gd = new GammaDistribution(new MersenneTwister(SEED),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        IntList tt;
        for (int i = 0; i < 21; i++) {
            tt = new IntArrayList();
            for (int j = 0; j < n; j++) {
                tt.add((int) gd.sample());
            }
            LoggerFactory.getLogger(RESULT_CONSOLE_LOGGER)
                    .info(Arrays.toString(tt.toIntArray()));
        }
    }

    /**
     *
     */
    protected void runSingle() {
        final AbstractTimeSeriesImplementation profile;
        try {
            profile = CongestionProfile.createFromCSV(FILE,
                    COLUMN);
            final GammaDistribution gd = new GammaDistribution(
                    new MersenneTwister(SEED), R3DP_GAMMA_SHAPE,
                    R3DP_GAMMA_SCALE);
            for (int i = 0; i < n; i++) {
                final ExperimentInstance p = new ExperimentInstance(
                        getSolverBuilder(),
                        new DoubleArrayList(gd.sample(nagents)), profile,
                        ALLOW_LESS_ACTIVATIONS, TOTAL_PRODUCED_E);
                p.startExperiment();
                LoggerFactory.getLogger(RESULT_CONSOLE_LOGGER)
                        .info(String.valueOf(p.getEfficiency()));
            }
        } catch (final IOException e) {
            LoggerFactory.getLogger(ExperimentRunnerAllRes.class)
                    .error("IOException while opening profile.", e);
        }
    }

    protected String getLabel() {
        if (competitive) {
            return "comp";
        }
        return "coop";
    }

    protected void runBatch() {
        final CongestionProfile profile;
        final List<ExperimentAtom> instances = Lists.newArrayList();
        final GammaDistribution gd = new GammaDistribution(new MersenneTwister(SEED),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        try {
            profile = CongestionProfile.createFromCSV(FILE,
                    COLUMN);
            for (int i = 0; i < n; i++) {
                instances.add(new ExperimentAtomImplementation(
                        new DoubleArrayList(gd.sample(nagents)), profile));
            }
        } catch (final IOException e) {
            LoggerFactory.getLogger(ExperimentRunnerAllRes.class)
                    .error("IOException while opening profile.", e);
        }
        final ExperimentRunner r;
        if (RUN_MULTI_THREADED) {
            r = LocalRunners.createOSTunedMultiThreadedRunner();
        } else {
            r = LocalRunners.createDefaultSingleThreadedRunner();
        }
        r.runExperiments(instances);
    }

    protected void logResults() {
        final String builder = "BEGINRESULT:\n" + "Res1=" + mainRes1 +
                "\n" +
                "Res2=" + mainRes2 + "\n" +
                "ActRes1=" + actRes1 + "\n" +
                "ActRes2=" + actRes2 + "\n" +
                "ActEffRes1=" + actEffRes1 + "\n" +
                "ActEffRes2=" + actEffRes2 + "\n" +
                "SolvRes1=" + solvRes1 + "\n" +
                "SolvRes2=" + solvRes2 + "\n" +
                "RemediedRes1=" + remediedCong1 + "\n" +
                "RemediedRes2=" + remediedCong2 + "\n" +
                "Not meeting 40 acts: " +
                String.valueOf(n - mainRes1.size()) + "\n" +
                "Not meeting 40 acts: " +
                String.valueOf(n - mainRes2.size()) + "\n" +
                "ENDRESULT:\n";
        LoggerFactory.getLogger(RESULT_CONSOLE_LOGGER).info(builder);
    }

    protected synchronized void addMainResult(final String label, final double eff) {
        if ("comp".equals(label)) {
            mainRes1.add(eff);
        } else if ("coop".equals(label)) {
            mainRes2.add(eff);
        }
    }

    protected synchronized void addActResult(final String label, final double eff) {
        if ("comp".equals(label)) {
            actRes1.add(eff);
        } else if ("coop".equals(label)) {
            actRes2.add(eff);
        }
    }

    protected synchronized void addActEffResult(final String label, final double eff) {
        if ("comp".equals(label)) {
            actEffRes1.add(eff);
        } else if ("coop".equals(label)) {
            actEffRes2.add(eff);
        }
    }

    protected synchronized void addSolveResult(final String label, final double eff) {
        if ("comp".equals(label)) {
            solvRes1.add(eff);
        } else if ("coop".equals(label)) {
            solvRes2.add(eff);
        }
    }

    protected synchronized void addRemediedCongResult(final String label,
            final double eff) {
        if ("comp".equals(label)) {
            remediedCong1.add(eff);
        } else if ("coop".equals(label)) {
            remediedCong2.add(eff);
        }
    }

    protected SolverBuilder getSolverBuilder() {
        if (competitive) {
            return new CompetitiveSolverBuilder();
        }
        return new CooperativeSolverBuilder();
    }

    /**
     * @return the mainRes1
     */
    protected final List<Double> getMainRes1() {
        return Lists.newArrayList(mainRes1);
    }

    /**
     * @return the mainRes2
     */
    protected final List<Double> getMainRes2() {
        return Lists.newArrayList(mainRes2);
    }

    /**
     * @return the actRes1
     */
    protected final List<Double> getActRes1() {
        return Lists.newArrayList(actRes1);
    }

    /**
     * @return the actRes2
     */
    protected final List<Double> getActRes2() {
        return Lists.newArrayList(actRes2);
    }

    /**
     * @return the solvRes1
     */
    protected final List<Double> getSolvRes1() {
        return Lists.newArrayList(solvRes1);
    }

    /**
     * @return the solvRes2
     */
    protected final List<Double> getSolvRes2() {
        return Lists.newArrayList(solvRes2);
    }

    /**
     * @return the actEffRes1
     */
    protected final List<Double> getActEffRes1() {
        return Lists.newArrayList(actEffRes1);
    }

    /**
     * @return the actEffRes2
     */
    protected final List<Double> getActEffRes2() {
        return Lists.newArrayList(actEffRes2);
    }

    protected class ExperimentAtomImplementation extends ExperimentAtomImpl {
        @Nullable
        private final DoubleList real;
        @Nullable
        private ExperimentInstance p;
        private final @Nullable AbstractTimeSeriesImplementation profile;

        ExperimentAtomImplementation(final DoubleList realisation,
                final AbstractTimeSeriesImplementation profile) {
            this.real = new DoubleArrayList(realisation);
            this.profile = profile;
            doRegistration();
        }

        /**
         * This method will be called to finalize construction. Take considerable care when
         * overriding.
         */
        protected void doRegistration() {
            this.registerCallbackOnFinish(instance -> {
                addMainResult(getLabel(), checkNotNull(p).getEfficiency());
                addActResult(getLabel(), checkNotNull(p).getActivationRate());
                addActEffResult(getLabel(),
                        checkNotNull(p).getSummedAgentEfficiency());
                addSolveResult(getLabel(),
                        checkNotNull(p).getRemediedCongestionFraction());
                addRemediedCongResult(getLabel(), checkNotNull(p)
                        .getRemediedCongestionRelatedToProducedEnergy());
                p = null;
            });
        }

        private void start() {
            checkNotNull(p);
            p.startExperiment();
        }

        private void setup() {
            this.p = new ExperimentInstance(getSolverBuilder(),
                    checkNotNull(real), checkNotNull(profile),
                    ALLOW_LESS_ACTIVATIONS, TOTAL_PRODUCED_E);
        }

        @Override
        protected void execute() {
            setup();
            start();
        }
    }

    class CompetitiveSolverBuilder implements SolverBuilder {
        @Override
        public AbstractCongestionSolver getSolver(final AbstractTimeSeriesImplementation profile,
                final int n) {
            return new CompetitiveCongestionSolver(profile, 8, allowedExcess);
        }
    }

    class CooperativeSolverBuilder implements SolverBuilder {

        @Override
        public AbstractCongestionSolver getSolver(final AbstractTimeSeriesImplementation profile,
                final int n) {
            return new CooperativeCongestionSolver(profile, 8, allowedExcess);
        }
    }
}
