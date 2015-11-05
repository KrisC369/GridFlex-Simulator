/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
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
import be.kuleuven.cs.flexsim.experimentation.saso.RenumerationGameRunner;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunnerAEff {

    private int N = 1000;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final int PROGRESSBAR_LENGTH = 50;
    private int NAGENTS;
    private int ALLOWED_EXCESS = 33;
    private final List<Double> result1 = Lists.newCopyOnWriteArrayList();
    private final List<Double> result2 = Lists.newCopyOnWriteArrayList();
    private boolean competitive = true;
    private boolean allowLessActivations = true;

    private ExperimentRunnerAEff(int N, int nagents, int allowed) {
        this.N = N;
        this.NAGENTS = nagents;
        this.ALLOWED_EXCESS = allowed;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // GammaDistribution gd = new GammaDistribution(
        // new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
        // R3DP_GAMMA_SCALE);
        // int n = 3;
        // for (int i = 0; i < 21; i++) {
        // int[] t = new int[n];
        // for (int j = 0; j < n; j++) {
        // t[j] = (int) gd.sample();
        // }
        // System.out.println(Arrays.toString(t));
        // }

        if (args.length == 0) {
            new ExperimentRunnerAEff(10, 3, 33).execute();
        } else if (args.length == 1) {
            try {
                final int agents = Integer.valueOf(args[0]);
                new ExperimentRunnerAEff(1000, agents, 33).execute();
            } catch (Exception e) {
                LoggerFactory.getLogger(RenumerationGameRunner.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        } else if (args.length == 2) {
            try {
                final int agents = Integer.valueOf(args[1]);
                final int reps = Integer.valueOf(args[0]);
                new ExperimentRunnerAEff(reps, agents, 33).execute();
            } catch (Exception e) {
                LoggerFactory.getLogger(RenumerationGameRunner.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        } else if (args.length == 3) {
            try {
                final int agents = Integer.valueOf(args[1]);
                final int reps = Integer.valueOf(args[0]);
                final int allowed = Integer.valueOf(args[2]);
                new ExperimentRunnerAEff(reps, agents, allowed).execute();
            } catch (Exception e) {
                LoggerFactory.getLogger(RenumerationGameRunner.class)
                        .error("Unparseable cl parameters passed");
                throw e;
            }
        }
    }

    public void execute() {
        NormalDistribution gd = new NormalDistribution(
                new MersenneTwister(1312421l), 928.837, 933.529);
        double[] sample = gd.sample(NAGENTS);
        for (int i = 0; i < sample.length; i++) {
            sample[i] = sample[i] < 0 ? 0 : sample[i];
        }
        System.out.println(Arrays.toString(sample));
        // GammaDistribution gd = new GammaDistribution(
        // new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
        // R3DP_GAMMA_SCALE);
        // System.out.println(Arrays.toString(gd.sample(10000)));
        runBatch();
        competitive = false;
        runBatch();
        printResult();
        // er.runSingle();
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
                    new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                    R3DP_GAMMA_SCALE);
            for (int i = 0; i < N; i++) {
                ExperimentInstance p = (new ExperimentInstance(NAGENTS,
                        getSolverBuilder(), gd.sample(NAGENTS), profile,
                        allowLessActivations));
                p.startExperiment();
                System.out.println(p.getEfficiency());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SolverBuilder getSolverBuilder() {
        if (competitive) {
            return new CompetitiveSolverBuilder();
        }
        return new CooperativeSolverBuilder();
    }

    private String getLabel() {
        if (competitive) {
            return "comp";
        }
        return "coop";
    }

    public void runBatch() {
        CongestionProfile profile;
        List<ExperimentAtom> instances = Lists.newArrayList();
        GammaDistribution gd = new GammaDistribution(
                new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
        // NormalDistribution gd = new NormalDistribution(928.837, 933.529);
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
            for (int i = 0; i < N; i++) {
                instances.add(new ExperimentAtomImplementation(
                        gd.sample(NAGENTS), profile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ExperimentRunner r = LocalRunners.createOSTunedMultiThreadedRunner();
        // ExperimentRunner r = new SingleThreadedExperimentRunner();
        r.runExperiments(instances);
    }

    private void printResult() {
        System.out.println("BEGINRESULT:");
        System.out.println("Res1=" + result1);
        System.out.println("Res2=" + result2);
        System.out.println(
                "Not meeting 40 acts: " + String.valueOf(N - result1.size()));
        System.out.println(
                "Not meeting 40 acts: " + String.valueOf(N - result2.size()));
        System.out.println("ENDRESULT:");
    }

    private synchronized void addResult(String label, double eff) {
        if ("comp".equals(label)) {
            result1.add(eff);
        } else if ("coop".equals(label)) {
            result2.add(eff);
        }
    }

    class ExperimentAtomImplementation extends ExperimentAtomImpl {
        private @Nullable double[] real;
        private @Nullable ExperimentInstance p;
        private @Nullable CongestionProfile profile;

        ExperimentAtomImplementation(double[] realisation,
                CongestionProfile profile) {
            this.real = realisation;
            this.profile = profile;
            this.registerCallbackOnFinish(new ExperimentCallback() {

                @Override
                public void callback(ExperimentAtom instance) {
                    addResult(getLabel(),
                            checkNotNull(p).getSummedAgentEfficiency());
                    p = null;
                }
            });
        }

        private void start() {
            checkNotNull(p);
            p.startExperiment();
        }

        private void setup() {
            this.p = (new ExperimentInstance(NAGENTS, getSolverBuilder(),
                    checkNotNull(real), checkNotNull(profile),
                    allowLessActivations));
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
            return new CompetitiveCongestionSolver(profile, 8, ALLOWED_EXCESS);
        }
    }

    class CooperativeSolverBuilder implements SolverBuilder {

        @Override
        public AbstractCongestionSolver getSolver(CongestionProfile profile,
                int n) {
            return new CooperativeCongestionSolver(profile, 8, ALLOWED_EXCESS);
        }
    }
}
