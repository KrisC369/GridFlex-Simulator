package be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.transformation;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.MultiHorizonErrorGenerator;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.PowerValuesProfile;

/**
 * Abstract current profile to congestion profile converter.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
abstract class AbstractProfileConverter {
    static final double HOURS_PER_DAY = 24;
    static final double SLOTS_PER_HOUR = 4;
    static final double EPS = 0.00001;
    static final double DAY_AHEAD_NOMINATION_DEADLINE = 15;
    static final double PROFILE_START_TIME = 0;
    static final double TO_POWER = 1.73 * 15.6;
    static final double CONVERSION = 1.5d;

    private final PowerValuesProfile powerProfile;
    private final CableCurrentProfile profile;
    protected final MultiHorizonErrorGenerator random;

    AbstractProfileConverter(CableCurrentProfile profile,
            MultiHorizonErrorGenerator random) {
        this.profile = profile;
        this.random = random;
        this.powerProfile = PowerValuesProfile
                .createFromTimeSeries(profile.transform(p -> (p / CONVERSION) * TO_POWER));
    }

    /**
     * @return The conversion of the initial profile to an imbalance profile with only positive
     * values.
     */
    public CongestionProfile convertProfileToPositiveOnlyImbalanceVolumes() {
        return convertProfileToImbalanceVolumes().transform(v -> v > 0 ? v : 0);
    }

    /**
     * @return The conversion of the initial profile to an imbalance profile.
     */
    public CongestionProfile convertProfileToImbalanceVolumes() {
        return calculateImbalanceVolumeFromActualAndPredictedData(calculateForecastedProfile());
    }

    protected abstract PowerValuesProfile calculateForecastedProfile();

    /**
     * Calculate imbalance powerProfile from current and error sampled energy volumes.
     * To give the volumes that are unpredictably excessive, the predicted values are subtracted
     * from the actuals.
     *
     * @param tSPredicted the Predicted output volumes.
     * @return A power profile that represents forecast error induced imbalance.
     */
    CongestionProfile calculateImbalanceVolumeFromActualAndPredictedData(
            PowerValuesProfile tSPredicted) {
        //Don't forget to convert to given boosted profile.
        return CongestionProfile.createFromTimeSeries(
                getPowerProfile().subtractValues(tSPredicted).transform(p -> p * CONVERSION)
                        .transform(p -> p / SLOTS_PER_HOUR));
    }

    /**
     * Apply error sample based on the provided generator.
     *
     * @param idx   the time series index (as an indication of time).
     * @param value the actual value to apply error to.
     * @return The new value representing value + error sample.
     */
    final double applyErrorSampleToSingleValue(int idx, double value) {
        int errorGenIdx = (int) Math
                .ceil(((idx - PROFILE_START_TIME) % HOURS_PER_DAY) + (HOURS_PER_DAY
                        - DAY_AHEAD_NOMINATION_DEADLINE));
        return value + random.generateErrorForHorizon(errorGenIdx);
    }

    final CongestionProfile getOriginalCongestionProfile() {
        return CongestionProfile
                .createFromTimeSeries(powerProfile.transform(p -> p * CONVERSION / SLOTS_PER_HOUR));
    }

    final PowerValuesProfile getPowerProfile() {
        return powerProfile;
    }

    final CableCurrentProfile getProfile() {
        return profile;
    }

}
