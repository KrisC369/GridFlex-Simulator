/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;

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

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunnerSingle4 {

    private static int N = 500;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final int NAGENTS = 200;
    private final List<Double> result1 = Lists.newArrayList();
    private boolean competitive = false;
    private boolean allowLessActivations = true;

    /**
     * @param args
     */
    public static void main(String[] args) {
        ExperimentRunnerSingle4 er = new ExperimentRunnerSingle4();
        er.runBatch();
    }

    private SolverBuilder getSolverBuilder(int i) {
        if (competitive) {
            return new CompetitiveSolverBuilder(i);
        }
        return new CooperativeSolverBuilder(i);
    }

    public void runBatch() {
        List<ExperimentAtom> instances = Lists.newArrayList();
        for (int j = 0; j < NAGENTS; j++) {
            ExperimentAtomImplementation i = new ExperimentAtomImplementation(
                    new GammaDistribution(new MersenneTwister(1312421l),
                            R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE),
                    j);
            instances.add(i);
        }
        ExperimentRunner r = LocalRunners.createOSTunedMultiThreadedRunner();

        r.runExperiments(instances);
        System.out.println("distribution of eff = " + result1);
    }

    private synchronized void addResult(int agents, double eff) {
        result1.add(agents, eff);
        System.out.println("Result added for " + agents + " " + eff);
    }

    class ExperimentAtomImplementation extends ExperimentAtomImpl {
        private final GammaDistribution gd;
        private final int agents;
        private volatile int result = -1;

        ExperimentAtomImplementation(GammaDistribution gd, final int agents) {
            this.agents = agents;
            this.gd = gd;
            this.registerCallbackOnFinish(new ExperimentCallback() {

                @Override
                public void callback(ExperimentAtom instance) {
                    final int res = result;
                    addResult(agents, res);
                }
            });
        }

        private void start() {
            CongestionProfile profile;
            double[] result = new double[100];
            try {
                profile = (CongestionProfile) CongestionProfile.createFromCSV(
                        "4kwartOpEnNeer.csv", "verlies aan energie");
                GammaDistribution gd = new GammaDistribution(
                        new MersenneTwister(1312421l), R3DP_GAMMA_SHAPE,
                        R3DP_GAMMA_SCALE);
                for (int i = 0; i < N; i++) {
                    ExperimentInstance p = (new ExperimentInstance(agents,
                            getSolverBuilder(i / (N / 100)), gd.sample(agents),
                            profile, allowLessActivations));
                    p.startExperiment();
                    result[i / (N / 100)] += p.getEfficiency();
                    // System.out.println(p.getEfficiency());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 100; i++) {
                result[i] /= (N / 100);
            }
            // Max max = new Max();
            // max.setData(result);
            List<Double> reslist = Lists.newArrayList();
            for (double d : result) {
                reslist.add(d);
            }
            double maxVal = Double.NEGATIVE_INFINITY;
            int maxK = -1;
            for (int k = 0; k < 100; k++) {
                if (result[k] > maxVal) {
                    maxVal = result[k];
                    maxK = k;
                }
            }
            this.result = maxK;
        }

        private void setup() {
        }

        @Override
        protected void execute() {
            setup();
            start();
        }
    }

    class CompetitiveSolverBuilder implements SolverBuilder {
        int i;

        public CompetitiveSolverBuilder(int i) {
            this.i = i;
        }

        @Override
        public AbstractCongestionSolver getSolver(CongestionProfile profile,
                int n) {
            return new CompetitiveCongestionSolver(profile, 8, i);
        }
    }

    class CooperativeSolverBuilder implements SolverBuilder {
        int i;

        public CooperativeSolverBuilder(int i) {
            this.i = i;
        }

        @Override
        public AbstractCongestionSolver getSolver(CongestionProfile profile,
                int n) {
            return new CooperativeCongestionSolver(profile, 8, i);
        }
    }
}
