package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.WindSpeedProfile;

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
                toEnergyVolumes(applyPredictionErrors(toWindSpeed(c, turbineSpec)), turbineSpec),
                c);
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
     * @param windprofile the input wind speeds.
     * @return profile with wind energy volumes.
     */
    private static TimeSeries toEnergyVolumes(WindSpeedProfile windprofile,
            TurbineSpecification specs) {

        return CableCurrentProfile.createFromTimeSeries(windprofile
                .transform(p -> convertWindToPower(p, specs)));
    }

    private static double convertWindToPower(double p, TurbineSpecification specs) {
        //        if (p < specs.getRatedPower()) {
        double rest = p % 1;
        int idx = (int) p;
        double interval = specs.getPowerValues().get(idx + 1) - specs.getPowerValues().get(idx);
        return specs.getPowerValues().get(idx) + interval * rest;
        //        }else{
        //        }
        //        return p;
    }

    /**
     * Apply prediction erros taking into account different time horizons and
     *
     * @param timeSeries The input wind speeds
     * @return wind speeds with sample errors added to them
     */
    private static WindSpeedProfile applyPredictionErrors(WindSpeedProfile timeSeries) {
        return timeSeries;
    }

    /**
     * Transforms the given profile of energy volumes to estimated wind speeds needed to cause
     * these errors.
     *
     * @param c the energy volume profile
     * @return the wind speeds profile
     */
    private static WindSpeedProfile toWindSpeed(CableCurrentProfile c, TurbineSpecification specs) {
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
        return WindSpeedProfile.createFromTimeSeries(singlePower
                .transform(p -> convertSinglePowerToWind(p, upperMarker, specs)));
    }

    private static double convertSinglePowerToWind(double p, double upperMarker,
            TurbineSpecification specs) {
        int i = specs.getPowerValues().indexOf(specs.getRatedPower());
        if (p < specs.getRatedPower()) {
            int idx = 1;
            while (idx < i) {
                if (p < specs.getPowerValues().get(idx)) {
                    idx++;
                }
            }
            double margin = specs.getPowerValues().get(idx) - specs.getPowerValues()
                    .get(idx - 1);
            if (margin == 0) {
                return idx;
            } else {
                return idx + (p - specs.getPowerValues()
                        .get(idx - 1)) / (specs.getPowerValues().get(idx) - specs
                        .getPowerValues()
                        .get(idx - 1));
            }
        } else

        {
            int j = specs.getPowerValues().lastIndexOf(specs.getRatedPower());

            double perc = (p - specs.getRatedPower()) / (upperMarker - specs.getRatedPower());
            return i + StrictMath.floor(perc * (j - i));
        }
    }

}
