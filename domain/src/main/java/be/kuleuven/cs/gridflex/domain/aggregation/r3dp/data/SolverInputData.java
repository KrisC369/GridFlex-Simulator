package be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data;

import be.kuleuven.cs.gridflex.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.gridflex.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.NetRegulatedVolumeProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.PositiveImbalancePriceProfile;
import com.google.auto.value.AutoValue;

/**
 * Input data value class for encapsulating data needed by r3dp solver instances.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class SolverInputData {
    /**
     * @return the cable current profile.
     */
    public abstract CableCurrentProfile getCableCurrentProfile();

    /**
     * @return the congested energy volume profile.
     */
    public abstract CongestionProfile getCongestionProfile();

    /**
     * @return the net regulated volume profile.
     */
    public abstract NetRegulatedVolumeProfile getNetRegulatedVolumeProfile();

    /**
     * @return the positive imbalance price profile.
     */
    public abstract PositiveImbalancePriceProfile getPositiveImbalancePriceProfile();

    /**
     * @return the tubine specification data sheet.
     */
    public abstract TurbineSpecification getTurbineSpecifications();

    /**
     * @return the forecast error distribution profile
     */ //TODO use forecast error instead, gen mheg locally.
    public abstract ForecastHorizonErrorDistribution getForecastHorizonErrorDistribution();

    /**
     * @return the day ahead price profile.
     */
    public abstract DayAheadPriceProfile getDayAheadPriceProfile();

    /**
     * @return the seed to use.
     */
    public abstract long getSeed();

    /**
     * Static factory method.
     *
     * @param ccp
     * @param cp
     * @param nrv
     * @param specs
     * @param distribution
     * @param pip
     * @param dap
     * @param seed
     * @return An autoValue solver data input file.
     */
    public static SolverInputData create(CableCurrentProfile ccp, CongestionProfile cp,
            NetRegulatedVolumeProfile nrv, TurbineSpecification specs,
            ForecastHorizonErrorDistribution distribution, PositiveImbalancePriceProfile pip,
            DayAheadPriceProfile dap, long seed) {
        return new AutoValue_SolverInputData(ccp, cp, nrv, pip, specs, distribution, dap, seed);
    }
}
