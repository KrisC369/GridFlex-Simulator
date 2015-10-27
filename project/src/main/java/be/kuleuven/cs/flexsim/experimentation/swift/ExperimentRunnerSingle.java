/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import autovalue.shaded.com.google.common.common.collect.Lists;
import be.kuleuven.cs.flexsim.domain.energy.dso.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CompetitiveCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CooperativeCongestionSolver;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentCallback;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.runners.local.SingleThreadedExperimentRunner;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunnerSingle {

    private static int N = 100;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final int NAGENTS = 10;
    private static final int ALLOWED_EXCESS = 45;
    private final List<Double> result1 = Lists.newArrayList();
    private final List<Double> result2 = Lists.newArrayList();
    private boolean competitive = true;

    /**
     * @param args
     */
    public static void main(String[] args) {
        ExperimentRunnerSingle er = new ExperimentRunnerSingle();
        // er.runBatch();
        er.runSingle();
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
                        getSolverBuilder(), gd, profile));
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
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
            for (int i = 0; i < N; i++) {
                instances.add(new ExperimentAtomImplementation(gd.sample(10),
                        profile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // ExperimentRunner r = new MultiThreadedExperimentRunner(8);
        ExperimentRunner r = new SingleThreadedExperimentRunner();

        r.runExperiments(instances);
        System.out.println(result1);
    }

    private synchronized void addResult(String lable, double eff) {
        if ("comp".equals(lable)) {
            result1.add(eff);
        } else if ("coop".equals(lable)) {
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
                    addResult(getLabel(), checkNotNull(p).getEfficiency());
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
                    checkNotNull(real), checkNotNull(profile)));
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
