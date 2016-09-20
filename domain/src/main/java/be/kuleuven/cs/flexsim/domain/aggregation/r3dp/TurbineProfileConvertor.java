package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.PowerValuesProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.WindSpeedProfile;

/**
 * Convertor class for converting current profiles to imbalance profiles.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class TurbineProfileConvertor {
    static final double TO_POWER = 1.73 * 15.6;
    private static final double CONVERSION = 1.5d;
    private static final double HOURS_PER_DAY = 24;
    private final PowerValuesProfile powerProfile;
    private final PowerValuesProfile singleTProfile;
    private final TurbineSpecification specs;
    private final int nbTurbines;
    private final double maxPSingle;
    private final WindErrorGenerator random;
    public static final double DAY_AHEAD_NOMINATION_DEADLINE = 15;
    public static final double PROFILE_START_TIME = 0;

    /**
     * Default Constructor.
     *
     * @param profile      The powerProfile to convert.
     * @param specs        The turbine specs to use.
     * @param distribution
     * @param random
     */
    public TurbineProfileConvertor(CableCurrentProfile profile, TurbineSpecification specs,
            WindErrorGenerator random) {
        this.specs = specs;
        this.powerProfile = PowerValuesProfile
                .createFromTimeSeries(profile.transform(p -> (p / CONVERSION) * TO_POWER));
        this.random = random;
        double maxPFound = powerProfile.max();
        this.nbTurbines = (int) Math.floor(maxPFound / specs.getRatedPower());
        this.maxPSingle = maxPFound / nbTurbines;
        this.singleTProfile = PowerValuesProfile
                .createFromTimeSeries(powerProfile.transform(p -> p / (double) nbTurbines));
    }

    /**
     * @return The conversion of the initial profile to an imbalance profile.
     */
    public final PowerValuesProfile convertProfileWith() {
        return calculateImbalanceFromActual(
                toPowerValues(applyPredictionErrors(toWindSpeed())));
    }

    /**
     * Calculate imbalance powerProfile from current and error sampled energy volumes.
     *
     * @param tSPredicted  the Predicted output volumes.
     * @param tSCongestion the actual output volumes.
     * @return
     */
    PowerValuesProfile calculateImbalanceFromActual(PowerValuesProfile tSPredicted) {
        return PowerValuesProfile.createFromTimeSeries(powerProfile.subtractValues(tSPredicted));
    }

    /**
     * Convert wind speeds to energy volume powerProfile using nominal wind production power values.
     *
     * @param windprofile the input wind speeds.
     * @return powerProfile with wind energy volumes.
     */
    PowerValuesProfile toPowerValues(WindSpeedProfile windprofile) {
        return PowerValuesProfile.createFromTimeSeries(windprofile
                .transform(p -> convertWindToPower(p)).transform(p -> p * nbTurbines)
                .transform(p -> p * CONVERSION));
    }

    /**
     * Apply prediction erros taking into account different time horizons and
     *
     * @param timeSeries         The input wind speeds
     * @param error_distribution
     * @return wind speeds with sample errors added to them
     */
    private WindSpeedProfile applyPredictionErrors(WindSpeedProfile timeSeries) {
        //TODO fix round to zero
        return WindSpeedProfile.createFromTimeSeries(timeSeries.transformFromIndex(
                i -> timeSeries.value(i) + applyErrorSingleError(i, timeSeries.value(i)))
                .transform(w -> w < 0 ? 0 : w));
    }

    private double applyErrorSingleError(int idx, double value) {
        int errorGenIdx = (int) Math
                .ceil(((idx - PROFILE_START_TIME) % HOURS_PER_DAY) + (HOURS_PER_DAY
                        - DAY_AHEAD_NOMINATION_DEADLINE));
        return random.generateErrorForHorizon(errorGenIdx);
    }

    /**
     * Transforms the given powerProfile of energy volumes to estimated wind speeds needed to cause
     * these errors.
     *
     * @param c the energy volume powerProfile
     * @return the wind speeds powerProfile
     */
    WindSpeedProfile toWindSpeed() {
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
