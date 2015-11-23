/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentCallback;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.flexsim.experimentation.runners.local.LocalRunners;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/**
 * Runner for batch experiments on allowed activation rate values.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunnerAllowedExcessAllValues
        extends ExperimentRunnerAllowedExcessSingleValue {

    private static final long SEED = 1312421l;
    private static final int N = 500;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final boolean COMPETITIVE = false;
    private static final int NAGENTS = 200;
    private final double[] result2 = new double[NAGENTS];

    private final boolean allowLessActivations = true;

    /**
     * @param args
     *            stdin args.
     */
    public static void main(String[] args) {
        ExperimentRunnerAllowedExcessAllValues er = new ExperimentRunnerAllowedExcessAllValues();
        er.runBatch();
    }

    @Override
    public void runBatch() {
        List<ExperimentAtom> instances = Lists.newArrayList();
        for (int j = 0; j < NAGENTS; j++) {
            ExperimentAtomImplementation i = new ExperimentAtomImplementation(
                    j);
            instances.add(i);
        }
        ExperimentRunner r = LocalRunners.createOSTunedMultiThreadedRunner();

        r.runExperiments(instances);
        System.out.println("distribution of eff = " + Arrays.toString(result2));
    }

    private void addResult(int agents, double eff) {
        synchronized (result2) {
            result2[agents] = eff;
        }
        System.out.println("Result added for " + agents + " " + eff);
    }

    class ExperimentAtomImplementation extends ExperimentAtomImpl {
        private final int agents;
        private volatile int result = -1;

        ExperimentAtomImplementation(final int agents) {
            this.agents = agents;
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
            double[] localResult = new double[100];
            try {
                profile = (CongestionProfile) CongestionProfile.createFromCSV(
                        "4kwartOpEnNeer.csv", "verlies aan energie");
                GammaDistribution gd = new GammaDistribution(
                        new MersenneTwister(SEED), R3DP_GAMMA_SHAPE,
                        R3DP_GAMMA_SCALE);
                for (int i = 0; i < N; i++) {
                    ExperimentInstance p = new ExperimentInstance(
                            getSolverBuilder(COMPETITIVE,
                                    (int) (i / (N / 100.0))),
                            new DoubleArrayList(gd.sample(agents)), profile,
                            allowLessActivations);
                    p.startExperiment();
                    localResult[i / (N / 100)] += p.getEfficiency();
                }
            } catch (IOException e) {
                LoggerFactory
                        .getLogger(ExperimentRunnerAllowedExcessAllValues.class)
                        .error("IOException while opening profile.", e);
            }
            for (int i = 0; i < 100; i++) {
                localResult[i] /= (N / 100.0);
            }
            List<Double> reslist = Lists.newArrayList();
            for (double d : localResult) {
                reslist.add(d);
            }
            double maxVal = Double.NEGATIVE_INFINITY;
            int maxK = -1;
            for (int k = 0; k < 100; k++) {
                if (localResult[k] > maxVal) {
                    maxVal = localResult[k];
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
}
