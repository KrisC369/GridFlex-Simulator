package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.domain.energy.dso.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CompetitiveCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CooperativeCongestionSolver;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentCallback;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.runners.local.LocalRunners;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunnerAllRes implements ExecutableExperiment {

    private static final long SEED = 1312421l;
    private static final boolean RUN_MULTI_THREADED = true;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final int N = 1000;
    private static final int ALLOWED_EXCESS = 33;
    private static final boolean ALLOW_LESS_ACTIVATIONS = true;
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
    private boolean competitive = true;

    protected ExperimentRunnerAllRes(int n, int nagents, int allowed) {
        this.n = n;
        this.nagents = nagents;
        this.allowedExcess = allowed;
    }

    /**
     * @param args
     *            StdIn args.
     */
    public static void main(String[] args) {
        ExpGenerator gen = new ExpGenerator() {

            @Override
            public ExecutableExperiment getExperiment(int reps, int agents,
                    int allowed) {
                return new ExperimentRunnerAllRes(reps, agents, allowed);
            }
        };
        parseInput(gen, args, N, ALLOWED_EXCESS);
    }

    protected static void parseInput(ExpGenerator gen, String[] args, int n,
            int allowedEx) {
        if (args.length == 0) {
            startExperiment(gen, 10, 81, allowedEx);
        } else if (args.length == 1) {
            try {
                final int agents = Integer.valueOf(args[0]);
                startExperiment(gen, n, agents, allowedEx);
            } catch (Exception e) {
                LoggerFactory.getLogger(ExperimentRunnerAllRes.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        } else if (args.length == 2) {
            try {
                final int agents = Integer.valueOf(args[1]);
                final int reps = Integer.valueOf(args[0]);
                startExperiment(gen, reps, agents, allowedEx);
            } catch (Exception e) {
                LoggerFactory.getLogger(ExperimentRunnerAllRes.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        } else if (args.length == 3) {
            try {
                final int agents = Integer.valueOf(args[1]);
                final int reps = Integer.valueOf(args[0]);
                final int allowed = Integer.valueOf(args[2]);
                startExperiment(gen, reps, agents, allowed);
            } catch (Exception e) {
                LoggerFactory.getLogger(ExperimentRunnerAllRes.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        }

    }

    static void startExperiment(ExpGenerator gen, int reps, int agents,
            int allowed) {
        gen.getExperiment(reps, agents, allowed).execute();
    }

    /**
     * Execute experiments
     */
    public void execute() {
        runBatch();
        competitive = false;
        runBatch();
        printResult();
    }

    @SuppressWarnings("unused")
    private static void generateRates(int n) {
        GammaDistribution gd = new GammaDistribution(new MersenneTwister(SEED),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        for (int i = 0; i < 21; i++) {
            IntList tt = new IntArrayList();
            for (int j = 0; j < n; j++) {
                tt.add((int) gd.sample());
            }
            System.out.println(Arrays.toString(tt.toIntArray()));
        }
    }

    /**
     * 
     */
    protected void runSingle() {
        CongestionProfile profile;
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
            GammaDistribution gd = new GammaDistribution(
                    new MersenneTwister(SEED), R3DP_GAMMA_SHAPE,
                    R3DP_GAMMA_SCALE);
            for (int i = 0; i < n; i++) {
                ExperimentInstance p = (new ExperimentInstance(
                        getSolverBuilder(),
                        new DoubleArrayList(gd.sample(nagents)), profile,
                        ALLOW_LESS_ACTIVATIONS));
                p.startExperiment();
                System.out.println(p.getEfficiency());
            }
        } catch (IOException e) {
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
        CongestionProfile profile;
        List<ExperimentAtom> instances = Lists.newArrayList();
        GammaDistribution gd = new GammaDistribution(new MersenneTwister(SEED),
                R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
            for (int i = 0; i < n; i++) {
                instances.add(new ExperimentAtomImplementation(
                        new DoubleArrayList(gd.sample(nagents)), profile));
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(ExperimentRunnerAllRes.class)
                    .error("IOException while opening profile.", e);
        }
        ExperimentRunner r;
        if (RUN_MULTI_THREADED) {
            r = LocalRunners.createOSTunedMultiThreadedRunner();
        } else {
            r = LocalRunners.createDefaultSingleThreadedRunner();
        }
        r.runExperiments(instances);
    }

    protected void printResult() {
        System.out.println("BEGINRESULT:");
        System.out.println("Res1=" + mainRes1);
        System.out.println("Res2=" + mainRes2);
        System.out.println("ActRes1=" + actRes1);
        System.out.println("ActRes2=" + actRes2);
        System.out.println("ActEffRes1=" + actEffRes1);
        System.out.println("ActEffRes2=" + actEffRes2);
        System.out.println("SolvRes1=" + solvRes1);
        System.out.println("SolvRes2=" + solvRes2);
        System.out.println(
                "Not meeting 40 acts: " + String.valueOf(n - mainRes1.size()));
        System.out.println(
                "Not meeting 40 acts: " + String.valueOf(n - mainRes2.size()));
        System.out.println("ENDRESULT:");
    }

    protected synchronized void addMainResult(String label, double eff) {
        if ("comp".equals(label)) {
            mainRes1.add(eff);
        } else if ("coop".equals(label)) {
            mainRes2.add(eff);
        }
    }

    protected synchronized void addActResult(String label, double eff) {
        if ("comp".equals(label)) {
            actRes1.add(eff);
        } else if ("coop".equals(label)) {
            actRes2.add(eff);
        }
    }

    protected synchronized void addActEffResult(String label, double eff) {
        if ("comp".equals(label)) {
            actEffRes1.add(eff);
        } else if ("coop".equals(label)) {
            actEffRes2.add(eff);
        }
    }

    protected synchronized void addSolveResult(String label, double eff) {
        if ("comp".equals(label)) {
            solvRes1.add(eff);
        } else if ("coop".equals(label)) {
            solvRes2.add(eff);
        }
    }

    protected class ExperimentAtomImplementation extends ExperimentAtomImpl {
        @Nullable
        private final DoubleList real;
        @Nullable
        private ExperimentInstance p;
        @Nullable
        private final CongestionProfile profile;

        ExperimentAtomImplementation(DoubleList realisation,
                CongestionProfile profile) {
            this.real = realisation;
            this.profile = profile;
            doRegistration();
        }

        protected void doRegistration() {
            this.registerCallbackOnFinish(new ExperimentCallback() {

                @Override
                public void callback(ExperimentAtom instance) {
                    addMainResult(getLabel(), checkNotNull(p).getEfficiency());
                    addActResult(getLabel(),
                            checkNotNull(p).getActivationRate());
                    addActEffResult(getLabel(),
                            checkNotNull(p).getSummedAgentEfficiency());
                    addSolveResult(getLabel(),
                            checkNotNull(p).getRemediedCongestionFraction());

                    p = null;
                }
            });
        }

        private void start() {
            checkNotNull(p);
            p.startExperiment();
        }

        private void setup() {
            this.p = new ExperimentInstance(getSolverBuilder(),
                    checkNotNull(real), checkNotNull(profile),
                    ALLOW_LESS_ACTIVATIONS);
        }

        @Override
        protected void execute() {
            setup();
            start();
        }
    }

    class CompetitiveSolverBuilder implements SolverBuilder {
        @Override
        public AbstractCongestionSolver getSolver(CongestionProfile profile,
                int n) {
            return new CompetitiveCongestionSolver(profile, 8, allowedExcess);
        }
    }

    class CooperativeSolverBuilder implements SolverBuilder {

        @Override
        public AbstractCongestionSolver getSolver(CongestionProfile profile,
                int n) {
            return new CooperativeCongestionSolver(profile, 8, allowedExcess);
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
}
