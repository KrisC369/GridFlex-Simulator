package be.kuleuven.cs.gametheory.evolutionary;

import be.kuleuven.cs.gametheory.results.HeuristicSymmetricPayoffMatrix;
import be.kuleuven.cs.gametheory.results.PayoffEntry;
import be.kuleuven.cs.gametheory.stats.ConfidenceLevel;
import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static java.lang.StrictMath.sqrt;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class EvolutionaryGameDynamics {

    private static final Logger logger = LoggerFactory.getLogger(EvolutionaryGameDynamics.class);
    private final HeuristicSymmetricPayoffMatrix payoffs;
    private final List<Double> eqnFactorMeans;
    private final List<Double> eqnFactorStds;
    private final List<Integer> eqnFactorSamples;

    private EvolutionaryGameDynamics(HeuristicSymmetricPayoffMatrix payoffs) {
        this.payoffs = payoffs;
        eqnFactorMeans = Lists.newArrayList();
        eqnFactorStds = Lists.newArrayList();
        eqnFactorSamples = Lists.newArrayList();
        calculateFactors(payoffs, eqnFactorMeans, eqnFactorStds, eqnFactorSamples);
    }

    /**
     * Generate all unique coefficients that are used for specifying dynamics
     * equations.
     *
     * @return A list of coefficients.
     */
    public List<Double> getDynamicEquationFactors() {
        return Lists.newArrayList(eqnFactorMeans);
    }

    /**
     * Generate the standard deviation coefficients for these dynamics.
     *
     * @return A list of standard deviations.
     */
    public List<Double> getDynamicEquationStds() {
        return Lists.newArrayList(eqnFactorStds);
    }

    public List<ConfidenceInterval> getConfidenceIntervals(ConfidenceLevel level) {
        List<ConfidenceInterval> cis = Lists.newArrayList();
        for (int i = 0; i < eqnFactorMeans.size(); i++) {
            double mean = eqnFactorMeans.get(i);
            double std = eqnFactorStds.get(i);
            int sampleSize = eqnFactorSamples.get(i);
            //hack to allow creating CI's
            if (std == 0) {
                std += 0.00001;
            }
            double error = level.getConfideneCoeff() * std / sqrt((double) sampleSize);
            cis.add(new ConfidenceInterval(mean - error, mean + error, level.getConfidenceLevel()));
        }
        return cis;
    }

    private void calculateFactors(HeuristicSymmetricPayoffMatrix payoffs,
            List<Double> means, List<Double> stds, List<Integer> samples) {

        for (final Map.Entry<PayoffEntry, Double[]> e : payoffs) {
            final PayoffEntry entry = e.getKey();
            final Double[] values = payoffs.getEntry(e.getKey().getEntries());
            final Double[] vars = payoffs.getVariance(e.getKey().getEntries());

            int coeffDone = 0;
            for (int currCoeff : entry.getEntries()) {
                sumSimilarAgentPayoffs(means, stds, samples, values, vars, coeffDone, currCoeff,
                        payoffs.getEntryCount(entry));
                coeffDone += currCoeff;
            }
        }
    }

    /**
     * @param means      the means list to store results into.
     * @param stds       the stds list to store results into.
     * @param samples    the samples N amount list to store results into.
     * @param values     The entry means to encorporate into the results
     * @param vars       The entry vars to encorporate into the results.
     * @param coeffDone  The coefficient count already seen.
     * @param currCoeff  The current number of agents to consider.
     * @param sampleSize The number of samples.
     */
    private static void sumSimilarAgentPayoffs(List<Double> means, List<Double> stds,
            List<Integer> samples, Double[] values, Double[] vars, int coeffDone, int currCoeff,
            int sampleSize) {
        Mean meanOfMeans = new Mean();
        Mean meanOfVars = new Mean();
        for (int j = coeffDone; j < coeffDone + currCoeff; j++) {
            meanOfMeans.increment(values[j]);
            meanOfVars.increment(vars[j]);
        }
        if (currCoeff > 0) {
            means.add(meanOfMeans.getResult());
            stds.add(Math.sqrt(meanOfVars.getResult()));
            samples.add(sampleSize * currCoeff);
        }
    }

    /**
     * Returns the parameters of the dynamics in a formatted string.
     *
     * @return the params in a MATLAB style formatted string.
     */
    String getDynamicsParametersString() {
        final StringBuilder b = new StringBuilder(30);
        char character = 'a';
        b.append("\n");
        for (final Double d : getDynamicEquationFactors()) {
            b.append(character++).append("=").append(d).append(";\n");
        }
        return b.toString();
    }

    void logResults() {
        final StringBuilder b = new StringBuilder(30);
        b.append(getResultString()).append("\n")
                .append("Dynamics equation params:");
        for (final Double d : EvolutionaryGameDynamics.from(payoffs).getDynamicEquationFactors()) {
            b.append(d).append("\n");
        }
        logger.debug(b.toString());
    }

    String getResultString() {
        return payoffs.toString();
    }

    /**
     * Creates a EGD object from a specified payoff matrix.
     *
     * @param payoffs The payoff matrix.
     * @return The initialized EGD instance.
     */
    public static EvolutionaryGameDynamics from(HeuristicSymmetricPayoffMatrix payoffs) {
        return new EvolutionaryGameDynamics(payoffs);
    }

}
