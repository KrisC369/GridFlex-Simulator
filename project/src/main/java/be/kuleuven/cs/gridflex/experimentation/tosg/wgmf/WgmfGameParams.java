package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.SolverInputData;
import be.kuleuven.cs.gridflex.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.gridflex.domain.util.data.PowerForecastMultiHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.WindSpeedForecastMultiHorizonErrorDistribution;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.gridflex.experimentation.tosg.data.WindBasedInputData;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

/**
 * Input data parameters for Wgmf games.
 */
@AutoValue
public abstract class WgmfGameParams implements Serializable {
    /**
     * @return The input wind data profile to work from.
     */
    public abstract WindBasedInputData getInputData();

    /**
     * @return The specific solvers factory platform to use.
     */
    public abstract WgmfSolverFactory getFactory();

    /**
     * @return The specs of the windturbine used in these simulations.
     */
    public abstract TurbineSpecification getSpecs();

    /**
     * @return The distribution of wind errors to use.
     */
    public abstract WindSpeedForecastMultiHorizonErrorDistribution getWindSpeedErrorDistributions();

    /**
     * @return The distribution of wind errors to use.
     */
    public abstract PowerForecastMultiHorizonErrorDistribution getPowerErrorDistributions();

    /**
     * @return Imbalance price input data.
     */
    public abstract ImbalancePriceInputData getImbalancePriceData();

    /**
     * @return Day ahead market prices.
     */
    public abstract DayAheadPriceProfile getDayAheadPriceData();

    /**
     * Static factory method.
     *
     * @param inputData The data profile to work from.
     * @param specs     The specs of the windturbine used in these simulations.
     * @param imbalIn   The imbalance prices.
     * @param windDist  The distribution of wind errors to use.
     * @param factory   The specific solvers factory platform to use.
     * @return A parameter object.
     */
    public static WgmfGameParams create(WindBasedInputData inputData,
            WgmfSolverFactory factory, TurbineSpecification specs,
            WindSpeedForecastMultiHorizonErrorDistribution windDist,
            PowerForecastMultiHorizonErrorDistribution powerDist,
            ImbalancePriceInputData imbalIn,
            DayAheadPriceProfile dap) {
        return new AutoValue_WgmfGameParams(inputData, factory, specs, windDist, powerDist, imbalIn,
                dap);
    }

    /**
     * Convert to domain internal input data format.
     *
     * @param seed the seed to add.
     * @return A solver input data object.
     */
    public SolverInputData toSolverInputData(long seed) {
        return SolverInputData.create(getInputData().getCableCurrentProfile(),
                getInputData().getCongestionProfile(),
                getImbalancePriceData().getNetRegulatedVolumeProfile(), getSpecs(),
                getWindSpeedErrorDistributions(), getPowerErrorDistributions(),
                getImbalancePriceData().getPositiveImbalancePriceProfile(),
                getDayAheadPriceData(), seed);
    }
}
