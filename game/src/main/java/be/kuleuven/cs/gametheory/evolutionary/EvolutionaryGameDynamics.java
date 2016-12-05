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

    private EvolutionaryGameDynamics(HeuristicSymmetricPayoffMatrix payoffs) {
        this.payoffs = payoffs;
        eqnFactors = calculateFactors(payoffs);
    }

    /**
     * Generate all unique coefficients that are used for specifying dynamics
     * equations.
     *
     * @return A list of coefficients.
     */
    // TODO Refactor out this analysis specific data computation + calculate
    // other specs in the refactored out module.
    public List<Double> getDynamicEquationFactors() {
        return Lists.newArrayList(eqnFactors);
    }

    private List<Double> calculateFactors(HeuristicSymmetricPayoffMatrix payoffs) {
        final List<Double> toReturn = Lists.newArrayList();
        for (final Map.Entry<PayoffEntry, Double[]> e : payoffs) {
            final PayoffEntry entry = e.getKey();
            final Double[] values = payoffs.getEntry(e.getKey().getEntries());
            int coeffDone = 0;

            for (int currCoeff : entry.getEntries()) {
                long sum = 0;
                for (int j = coeffDone; j < coeffDone + currCoeff; j++) {
                    sum += values[j];
                }
                if (currCoeff > 0) {
                    final double avg = sum / (double) currCoeff;
                    toReturn.add(avg);
                }
                coeffDone += currCoeff;//TODO CHECK THIS LINE.
            }
        }
        return toReturn;
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
