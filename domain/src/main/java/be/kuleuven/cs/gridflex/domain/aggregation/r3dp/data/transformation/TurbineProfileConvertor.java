package be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.transformation;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.MultiHorizonErrorGenerator;
import be.kuleuven.cs.gridflex.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.PowerValuesProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.WindSpeedProfile;
import com.google.common.annotations.VisibleForTesting;

/**
 * Convertor class for converting current profiles to imbalance profiles by deaggregating to
 * single turbine level and then applying errors to inferred wind speeds.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class TurbineProfileConvertor extends AbstractProfileConvertor {
    private final PowerValuesProfile singleTProfile;
    private final TurbineSpecification specs;
    private final int nbTurbines;
    private final double maxPSingle;

    /**
     * Default Constructor.
     *
     * @param profile The powerProfile to convert.
     * @param specs   The turbine specs to use.
     * @param random  The random generator of wind errors for different horizons.
     */
    public TurbineProfileConvertor(CableCurrentProfile profile, TurbineSpecification specs,
            MultiHorizonErrorGenerator random) {
        super(profile, random);
        this.specs = specs;
        double maxPFound = getPowerProfile().max();
        this.nbTurbines = (int) Math.floor(maxPFound / specs.getRatedPower());
        this.maxPSingle = maxPFound / nbTurbines;
        this.singleTProfile = PowerValuesProfile
                .createFromTimeSeries(getPowerProfile().transform(p -> p / (double) nbTurbines));
    }

    /**
     * @return Power values profile with prediction/forecast data.
     */
    protected PowerValuesProfile calculateForecastedProfile() {
        return toPowerValues(applyPredictionErrors(toWindSpeed()));
    }

    /**
     * Convert wind speeds to energy volume powerProfile using nominal wind production power values.
     *
     * @param windprofile the input wind speeds.
     * @return powerProfile with wind energy volumes.
     */
    PowerValuesProfile toPowerValues(WindSpeedProfile windprofile) {
        return PowerValuesProfile.createFromTimeSeries(windprofile
                .transform(this::convertWindToPower).transform(p -> p * nbTurbines));
    }

    /**
     * Apply prediction erros taking into account different time horizons.
     *
     * @param timeSeries The input wind speeds
     * @return wind speeds with sample errors added to them
     */
    private WindSpeedProfile applyPredictionErrors(WindSpeedProfile timeSeries) {
        return WindSpeedProfile.createFromTimeSeries(timeSeries.transformFromIndex(
                i -> applyErrorSampleToSingleValue(i, timeSeries.value(i)))
                .transform(w -> w < 0 ? 0 : w));
    }

    /**
     * Transforms the given powerProfile of energy volumes to estimated wind speeds needed to cause
     * these errors.
     *
     * @return the wind speeds powerProfile
     */
    WindSpeedProfile toWindSpeed() {
        double upperMarker = maxPSingle;
        return WindSpeedProfile.createFromTimeSeries(singleTProfile
                .transform(p -> convertSingleTPowerToWind(p, upperMarker)));
    }

    private double convertWindToPower(double w) {
        int i = specs.getPowerValues().indexOf(specs.getRatedPower());
        if (w <= i) {
            double rest = w % 1;
            int idx = (int) w;
            double interval = 0;
            //if not 0.
            if (rest > EPS) {
                interval = specs.getPowerValues().get(idx + 1) - specs.getPowerValues().get(idx);
            }
            return specs.getPowerValues().get(idx) + interval * rest;
        } else {
            int j = specs.getPowerValues().lastIndexOf(specs.getRatedPower());
            double perc = (w - i) / (j - i);
            if (perc > 1) {
                //do cutoff above rated cutoff speeds
                return 0;
            }
            double margin = maxPSingle - specs.getRatedPower();
            return specs.getRatedPower() + (perc * margin);
        }
    }

    private double convertSingleTPowerToWind(double p, double upperMarker) {
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
            if (margin <= EPS) {
                return idx - 1d;
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

    /**
     * Visible for testing only.
     *
     * @return the predicted profile.
     */
    @VisibleForTesting
    CongestionProfile getPredictionCongestionProfile() {
        return CongestionProfile.createFromTimeSeries(
                toPowerValues(applyPredictionErrors(toWindSpeed()))
                        .transform(p -> p * CONVERSION / SLOTS_PER_HOUR));
    }
}
