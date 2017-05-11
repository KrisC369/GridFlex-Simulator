package be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.transformation;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.MultiHorizonErrorGenerator;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.PowerValuesProfile;

/**
 * Converter class for converting current profiles to imbalance profiles by applying forecast
 * errors on the power output level.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PowerForecastBasedConverter extends AbstractProfileConverter {

    /**
     * General constructor.
     *
     * @param cableCurrentProfile The cable current profile to start from.
     * @param gen                 Error generator serving as random generator.
     */
    public PowerForecastBasedConverter(CableCurrentProfile cableCurrentProfile,
            MultiHorizonErrorGenerator gen) {
        super(cableCurrentProfile, gen);
    }

    @Override
    protected PowerValuesProfile calculateForecastedProfile() {
        return applyPredictionErrors(getPowerProfile());
    }

    /**
     * Apply prediction erros taking into account different time horizons.
     *
     * @param timeSeries The input power values
     * @return power values with error samples added to them
     */
    private PowerValuesProfile applyPredictionErrors(PowerValuesProfile timeSeries) {
        return PowerValuesProfile.createFromTimeSeries(timeSeries.transformFromIndex(
                i -> applyErrorSampleToSingleValue(i, timeSeries.value(i)))
                .transform(w -> w < 0 ? 0 : w));
    }
}
