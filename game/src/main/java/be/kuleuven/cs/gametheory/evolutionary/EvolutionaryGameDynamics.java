package be.kuleuven.cs.gametheory.evolutionary;

import be.kuleuven.cs.gametheory.HeuristicSymmetricPayoffMatrix;
import be.kuleuven.cs.gametheory.PayoffEntry;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class EvolutionaryGameDynamics {

    private HeuristicSymmetricPayoffMatrix payoffs;
    private List<Double> eqnFactors;
    private List<Double> eqnFactorsStd;

    private EvolutionaryGameDynamics(HeuristicSymmetricPayoffMatrix payoffs) {
        this.payoffs = payoffs;
        eqnFactors = Lists.newArrayList();
        eqnFactorsStd = Lists.newArrayList();
        calculateFactors(payoffs, eqnFactors, eqnFactorsStd);
        //        calculateStds(payoffs, eqnFactorsStd);
    }

    /**
     * Generate all unique coefficients that are used for specifying dynamics
     * equations.
     *
     * @return A list of coefficients.
     */
    public List<Double> getDynamicEquationFactors() {
        return Lists.newArrayList(eqnFactors);
    }

    public List<Double> getDynamicEquationStds() {
        return Lists.newArrayList(eqnFactorsStd);
    }

    private void calculateFactors(HeuristicSymmetricPayoffMatrix payoffs,
            List<Double> means, List<Double> stds) {

        for (final Map.Entry<PayoffEntry, Double[]> e : payoffs) {
            final PayoffEntry entry = e.getKey();
            final Double[] values = payoffs.getEntry(e.getKey().getEntries());
            final Double[] vars = payoffs.getVariance(e.getKey().getEntries());

            int coeffDone = 0;
            for (int currCoeff : entry.getEntries()) {
                sumSimilarAgentPayoffs(means, stds, values, vars, coeffDone, currCoeff);
                coeffDone += currCoeff;
            }
        }
    }

    private static void sumSimilarAgentPayoffs(List<Double> means, List<Double> stds,
            Double[] values, Double[] vars,
            int coeffDone, int currCoeff) {
        long sum = 0;
        long varTotal = 0;
        for (int j = coeffDone; j < coeffDone + currCoeff; j++) {
            sum += values[j];
            varTotal += vars[j];
        }
        if (currCoeff > 0) {
            final double avg = sum / (double) currCoeff;
            final double avgVar = varTotal / (double) currCoeff;
            means.add(avg);
            stds.add(Math.sqrt(avgVar));
        }
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
