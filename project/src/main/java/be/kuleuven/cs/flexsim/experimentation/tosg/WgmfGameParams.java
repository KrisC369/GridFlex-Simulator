package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.WindBasedInputData;
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
     * @return The specific solver factory platform to use.
     */
    public abstract AbstractSolverFactory<SolutionResults> getFactory();

    /**
     * @return The specs of the windturbine used in these simulations.
     */
    public abstract TurbineSpecification getSpecs();

    /**
     * @return The distribution of wind errors to use.
     */
    public abstract ForecastHorizonErrorDistribution getDistribution();

    /**
     * @return
     */
    public abstract ImbalancePriceInputData getImbalIn();

    /**
     * Static factory method.
     *
     * @param inputData      The data profile to work from.
     * @param specs          The specs of the windturbine used in these simulations.
     * @param imbalIn        The imbalance prices.
     * @param distribution   The distribution of wind errors to use.
     * @param solverplatform The specific solver factory platform to use.
     * @return A parameter object.
     */
    public static WgmfGameParams create(WindBasedInputData inputData,
            AbstractSolverFactory<SolutionResults> factory, TurbineSpecification specs,
            ForecastHorizonErrorDistribution distribution, ImbalancePriceInputData imbalIn) {
        return new AutoValue_WgmfGameParams(inputData, factory, specs, distribution, imbalIn);
    }
}
