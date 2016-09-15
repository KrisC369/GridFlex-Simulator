package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
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
    private final TurbineSpecification turbineSpec;

    /**
     * Default constructor
     *
     * @param fac The solver factory to draw solver from.
     * @param c   The initial imbalance profile to transform to imbalances.
     */
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            CableCurrentProfile c, TurbineSpecification specs) {
        super(fac, c);
        this.turbineSpec = specs;
        imbalance = calculateImbalanceFromActual(
                toEnergyVolumes(applyPredictionErrors(toWindSpeed(c, turbineSpec))), c);
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
    private static TimeSeries toWindSpeed(CableCurrentProfile c, TurbineSpecification specs) {
        final double conversion = 1.5d;
        final double toPower = 1.73 * 15.6;

        //        double cP = 0;
        //        double rho = 0;
        //        double r = 0;//bladelength
        //        double A = Math.PI * Math.pow(r, 2);

        c.transform(p -> (p / conversion) * toPower);
        CableCurrentProfile aggregatedPower = CableCurrentProfile
                .createFromTimeSeries(c.transform(p -> (p / conversion) * toPower));
        double maxPFound = aggregatedPower.max();
        double nbTurbines = Math.floor(maxPFound / specs.getRatedPower());
        double maxPSingle = maxPFound / nbTurbines;

        double upperMarker = maxPSingle;
        double lowerMarker = specs.getRatedPower();

        CableCurrentProfile singlePower = CableCurrentProfile
                .createFromTimeSeries(aggregatedPower.transform(p -> p / nbTurbines));
        return CableCurrentProfile.createFromTimeSeries(singlePower
                .transform(p -> convertSinglePowerToWind(p, lowerMarker, upperMarker, specs)));
    }

    private static double convertSinglePowerToWind(double p, double lowerMarker, double upperMarker,
            TurbineSpecification specs) {
        return p;
    }

}
