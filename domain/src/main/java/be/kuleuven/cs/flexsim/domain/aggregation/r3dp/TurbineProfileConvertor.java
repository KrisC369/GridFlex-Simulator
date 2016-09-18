package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.WindSpeedProfile;

/**
 * Convertor class for converting current profiles to imbalance profiles.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class TurbineProfileConvertor {
    static final double TO_POWER = 1.73 * 15.6;
    private static final double CONVERSION = 1.5d;
    private final CableCurrentProfile profile;
    private final CableCurrentProfile singleTProfile;
    private final TurbineSpecification specs;
    private int nbTurbines;
    private double maxPSingle;

    /**
     * Default Constructor.
     *
     * @param profile The profile to convert.
     * @param specs   The turbine specs to use.
     */
    public TurbineProfileConvertor(CableCurrentProfile profile, TurbineSpecification specs) {
        this.profile = profile;
        this.specs = specs;
        CableCurrentProfile aggregatedPower = CableCurrentProfile
                .createFromTimeSeries(profile.transform(p -> (p / CONVERSION) * TO_POWER));
        double maxPFound = aggregatedPower.max();
        this.nbTurbines = (int) Math.floor(maxPFound / specs.getRatedPower());
        this.maxPSingle = maxPFound / nbTurbines;
        this.singleTProfile = CableCurrentProfile
                .createFromTimeSeries(aggregatedPower.transform(p -> p / (double) nbTurbines));
    }

    public final CableCurrentProfile convertProfileWith() {
        return calculateImbalanceFromActual(
                toEnergyVolumes(applyPredictionErrors(toWindSpeed())));
    }

    /**
     * Calculate imbalance profile from current and error sampled energy volumes.
     *
     * @param tSPredicted  the Predicted output volumes.
     * @param tSCongestion the actual output volumes.
     * @return
     */
    private CableCurrentProfile calculateImbalanceFromActual(TimeSeries tSPredicted) {
        //subtract from orig.
        return CableCurrentProfile.createFromTimeSeries(tSPredicted);
    }

    /**
     * Convert wind speeds to energy volume profile using nominal wind production power values.
     *
     * @param windprofile the input wind speeds.
     * @return profile with wind energy volumes.
     */
    private TimeSeries toEnergyVolumes(WindSpeedProfile windprofile) {
        return CableCurrentProfile.createFromTimeSeries(windprofile
                .transform(p -> convertWindToPower(p)).transform(p -> p * nbTurbines)
                .transform(p -> p * CONVERSION));
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
    private WindSpeedProfile toWindSpeed() {
        double upperMarker = maxPSingle;
        return WindSpeedProfile.createFromTimeSeries(singleTProfile
                .transform(p -> convertSinglePowerToWind(p, upperMarker)));
    }

    private double convertWindToPower(double w) {
        int i = specs.getPowerValues().indexOf(specs.getRatedPower());
        if (w <= i) {
            double rest = w % 1;
            int idx = (int) w;
            double interval = 0;
            if (rest != 0) {
                interval = specs.getPowerValues().get(idx + 1) - specs.getPowerValues().get(idx);
            }
            return (specs.getPowerValues().get(idx) + interval * rest);
        } else {
            int j = specs.getPowerValues().lastIndexOf(specs.getRatedPower());
            double margin = maxPSingle - specs.getRatedPower();
            double perc = (w - i) / (j - i);
            return specs.getRatedPower() + (perc * margin);
        }
    }

    private double convertSinglePowerToWind(double p, double upperMarker) {
        int i = specs.getPowerValues().indexOf(specs.getRatedPower());
        if (p < specs.getRatedPower()) {
            int idx = 1;
            while (idx < i) {
                if (p > specs.getPowerValues().get(idx)) {
                    idx++;
                } else {
                    break;
                }
            }
            double margin = specs.getPowerValues().get(idx) - specs.getPowerValues()
                    .get(idx - 1);
            if (margin == 0) {
                return idx - 1;
            } else {
                return (idx - 1) + (p - specs.getPowerValues()
                        .get(idx - 1)) / (specs.getPowerValues().get(idx) - specs
                        .getPowerValues().get(idx - 1));
            }
        } else {
            int j = specs.getPowerValues().lastIndexOf(specs.getRatedPower());

            double perc = (p - specs.getRatedPower()) / (upperMarker - specs.getRatedPower());
            return i + StrictMath.floor(perc * (j - i));
        }
    }
}
