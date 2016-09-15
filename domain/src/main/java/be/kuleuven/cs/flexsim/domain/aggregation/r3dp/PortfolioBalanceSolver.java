package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.util.data.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;

/**
 * Represents a portfolio balancing entity that solves intraday imbalances because of prediction
 * error in portfolios.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolver extends DistributionGridCongestionSolver {

    private final CableCurrentProfile imbalance;

    /**
     * Default constructor
     *
     * @param fac The solver factory to draw solver from.
     * @param c   The initial imbalance profile to transform to imbalances.
     */
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            CableCurrentProfile c) {
        super(fac, c);
        imbalance = calculateImbalanceFromActual(
                toEnergyVolumes(applyPredictionErrors(toWindSpeed(c))), c);
    }

    /**
     * Calculate imbalance profile from current and error sampled energy volumes.
     *
     * @param tSPredicted  the Predicted output volumes.
     * @param tSCongestion the actual output volumes.
     * @return
     */
    private static CableCurrentProfile calculateImbalanceFromActual(TimeSeries tSPredicted,
            CableCurrentProfile tSCongestion) {
        return CableCurrentProfile.createFromTimeSeries(tSPredicted);
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
    private static TimeSeries toWindSpeed(CableCurrentProfile c) {

        double cP = 0;
        double rho = 0;
        double r = 0;//bladelength
        double A = Math.PI * Math.pow(r, 2);

        for (double p : c.values()) {
            double arg = 2 * p / (A * rho * cP);
            double speed = StrictMath.cbrt(arg);
        }

        return c;
    }

}
