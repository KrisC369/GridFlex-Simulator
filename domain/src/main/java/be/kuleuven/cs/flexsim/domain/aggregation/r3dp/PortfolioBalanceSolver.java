package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;

/**
 * Represents a portfolio balancing entity that solves intraday imbalances because of prediction
 * error in portfolios.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolver extends DistributionGridCongestionSolver {

    private final CongestionProfile congestion;

    /**
     * Default constructor
     *
     * @param fac The solver factory to draw solver from.
     * @param c   The initial congestion profile to transform to imbalances.
     */
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            CongestionProfile c) {
        super(fac, c);
        congestion = calculateImbalanceFromActual(
                toEnergyVolumes(applyPredictionErrors(toWindSpeed(c))), c);
    }

    /**
     * Calculate imbalance profile from current and error sampled energy volumes.
     *
     * @param tSPredicted  the Predicted output volumes.
     * @param tSCongestion the actual output volumes.
     * @return
     */
    private static CongestionProfile calculateImbalanceFromActual(TimeSeries tSPredicted,
            CongestionProfile tSCongestion) {
        return CongestionProfile.createFromTimeSeries(tSPredicted);
    }

    /**
     * Convert wind speeds to energy volume profile using nominal wind production power values.
     *
     * @param timeSeries the input wind speeds.
     * @return profile with wind energy volumes.
     */
    private static TimeSeries toEnergyVolumes(TimeSeries timeSeries) {
        return timeSeries;
    }

    /**
     * Apply prediction erros taking into account different time horizons and
     *
     * @param timeSeries The input wind speeds
     * @return wind speeds with sample errors added to them
     */
    private static TimeSeries applyPredictionErrors(TimeSeries timeSeries) {
        return timeSeries;
    }

    /**
     * Transforms the given profile of energy volumes to estimated wind speeds needed to cause
     * these errors.
     *
     * @param c the energy volume profile
     * @return the wind speeds profile
     */
    private static TimeSeries toWindSpeed(CongestionProfile c) {
        return c;
    }
}
