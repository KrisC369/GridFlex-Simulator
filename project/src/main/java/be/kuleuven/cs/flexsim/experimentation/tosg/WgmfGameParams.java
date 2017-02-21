package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.WindBasedInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.jppf.WgmfSolverFactory;
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
    public abstract ForecastHorizonErrorDistribution getDistribution();

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
     * @param inputData    The data profile to work from.
     * @param specs        The specs of the windturbine used in these simulations.
     * @param imbalIn      The imbalance prices.
     * @param distribution The distribution of wind errors to use.
     * @param factory      The specific solvers factory platform to use.
     * @return A parameter object.
     */
    public static WgmfGameParams create(WindBasedInputData inputData,
            WgmfSolverFactory factory, TurbineSpecification specs,
            ForecastHorizonErrorDistribution distribution, ImbalancePriceInputData imbalIn,
            DayAheadPriceProfile dap) {
        return new AutoValue_WgmfGameParams(inputData, factory, specs, distribution, imbalIn, dap);
    }
}
