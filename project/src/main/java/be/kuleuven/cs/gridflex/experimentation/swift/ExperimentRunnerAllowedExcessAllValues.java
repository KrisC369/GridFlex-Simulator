/**
 *
 */
package be.kuleuven.cs.gridflex.experimentation.swift;

import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentAtomImpl;
import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentRunner;
import be.kuleuven.cs.gridflex.experimentation.runners.local.LocalRunners;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Runner for batch experiments on allowed activation rate values.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunnerAllowedExcessAllValues
        extends ExperimentRunnerAllowedExcessSingleValue {

    private static final long SEED = 1312421L;
    private static final int N = 500;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final boolean COMPETITIVE = false;
    private static final int NAGENTS = 200;
    private static final double TOTAL_PRODUCED_E = 36360.905;
    private static final boolean ALLOW_LESS_ACTIVATIONS = true;
    private final double[] result2 = new double[NAGENTS];

    /**
     * @param args stdin args.
     */
    public static void main(final String[] args) {
        final ExperimentRunnerAllowedExcessAllValues er = new
                ExperimentRunnerAllowedExcessAllValues();
        er.runBatch();
    }

    @Override
    public void runBatch() {
        final List<ExperimentAtom> instances = Lists.newArrayList();
        for (int j = 0; j < NAGENTS; j++) {
            final ExperimentAtomImplementation i = new ExperimentAtomImplementation(
                    j);
            instances.add(i);
        }
        final ExperimentRunner r = LocalRunners.createOSTunedMultiThreadedRunner();

        r.runExperiments(instances);
        LoggerFactory.getLogger(ExperimentRunnerAllowedExcessAllValues.class)
                .info("distribution of eff = " + Arrays.toString(result2));
    }

    private void addResult(final int agents, final double eff) {
        synchronized (result2) {
            result2[agents] = eff;
        }
        LoggerFactory.getLogger(ExperimentRunnerAllowedExcessAllValues.class)
                .info("Result added for " + agents + " " + eff);
    }

    class ExperimentAtomImplementation extends ExperimentAtomImpl {
        private final int agents;
        private volatile int result = -1;

        ExperimentAtomImplementation(final int agents) {
            this.agents = agents;
            this.registerCallbackOnFinish(instance -> {
                final int res = result;
                addResult(agents, res);
            });
        }

        private void start() {
            final CongestionProfile profile;
            final double[] localResult = new double[100];
            try {
                profile = CongestionProfile.createFromCSV(
                        "4kwartOpEnNeer.csv", "verlies aan energie");
                final GammaDistribution gd = new GammaDistribution(
                        new MersenneTwister(SEED), R3DP_GAMMA_SHAPE,
                        R3DP_GAMMA_SCALE);
                for (int i = 0; i < N; i++) {
                    final ExperimentInstance p = new ExperimentInstance(
                            getSolverBuilder(COMPETITIVE,
                                    (int) (i / (N / 100.0))),
                            new DoubleArrayList(gd.sample(agents)), profile,
                            ALLOW_LESS_ACTIVATIONS, TOTAL_PRODUCED_E);
                    p.startExperiment();
                    localResult[i / (N / 100)] += p.getEfficiency();
                }
            } catch (final IOException e) {
                LoggerFactory
                        .getLogger(ExperimentRunnerAllowedExcessAllValues.class)
                        .error("IOException while opening profile.", e);
            }
            for (int i = 0; i < 100; i++) {
                localResult[i] /= (N / 100.0);
            }
            final List<Double> reslist = Lists.newArrayList();
            for (final double d : localResult) {
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
            //Nothing to do here.
        }

        @Override
        protected void execute() {
            setup();
            start();
        }
    }
}
