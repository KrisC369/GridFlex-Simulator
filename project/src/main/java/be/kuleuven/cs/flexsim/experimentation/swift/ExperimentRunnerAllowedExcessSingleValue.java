/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.CompetitiveCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.CooperativeCongestionSolver;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/**
 * * Runner for single threaded experiments on allowed activation rate value for
 * single param environment.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentRunnerAllowedExcessSingleValue
        extends ExperimentRunner1 {

    private static final long SEED = 1312421L;
    private static final int N = 100;
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final int NAGENTS = 200;
    private static final int ALLOWED_EXCESS = 33;
    private static final boolean ALLOW_LESS_ACTS = true;
    private static final boolean COMPETITIVE = false;
    private static final double TOTAL_PRODUCED_E = 36360.905;

    protected ExperimentRunnerAllowedExcessSingleValue() {
        super(N, NAGENTS, ALLOWED_EXCESS);
    }

    /**
     * @param args
     *            default stdin args
     */
    public static void main(final String[] args) {
        final ExperimentRunnerAllowedExcessSingleValue er = new ExperimentRunnerAllowedExcessSingleValue();
        er.runSingle();
    }

    @Override
    protected void runSingle() {
        final CongestionProfile profile;
        final double[] result = new double[100];
        try {
            profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
            final GammaDistribution gd = new GammaDistribution(
                    new MersenneTwister(SEED), R3DP_GAMMA_SHAPE,
                    R3DP_GAMMA_SCALE);
            for (int i = 0; i < N; i++) {
                final ExperimentInstance p = new ExperimentInstance(
                        getSolverBuilder(COMPETITIVE, (int) (i / (N / 100.0))),
                        new DoubleArrayList(gd.sample(NAGENTS)), profile,
                        ALLOW_LESS_ACTS, TOTAL_PRODUCED_E);
                p.startExperiment();
                result[(int) (i / (double) (N / 100))] += p.getEfficiency();
            }
        } catch (final IOException e) {
            LoggerFactory
                    .getLogger(ExperimentRunnerAllowedExcessSingleValue.class)
                    .error("IOException while opening profile.", e);
        }
        for (int i = 0; i < 100; i++) {
            result[i] /= (N / 100.0);
        }
        LoggerFactory.getLogger("CONSOLERESULT")
                .info("distribution of eff = " + Arrays.toString(result));
    }

    protected SolverBuilder getSolverBuilder(final boolean comp, final int i) {
        if (comp) {
            return new CompetitiveSolverBuilder(i);
        }
        return new CooperativeSolverBuilder(i);
    }

    static class CompetitiveSolverBuilder implements SolverBuilder {
        final int i;

        CompetitiveSolverBuilder(final int i) {
            this.i = i;
        }

        @Override
        public AbstractCongestionSolver getSolver(final CongestionProfile profile,
                final int n) {
            return new CompetitiveCongestionSolver(profile, 8, i);
        }
    }

    static class CooperativeSolverBuilder implements SolverBuilder {
        final int i;

        CooperativeSolverBuilder(final int i) {
            this.i = i;
        }

        @Override
        public AbstractCongestionSolver getSolver(final CongestionProfile profile,
                final int n) {
            return new CooperativeCongestionSolver(profile, 8, i);
        }
    }
}
