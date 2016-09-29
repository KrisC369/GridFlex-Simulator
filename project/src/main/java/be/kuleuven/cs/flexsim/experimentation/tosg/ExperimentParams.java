package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.ImbalancePriceInputData;
import be.kuleuven.cs.flexsim.experimentation.tosg.data.WindBasedInputData;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract class ExperimentParams implements Serializable {

    public abstract WindBasedInputData getInputData();

    public abstract AbstractSolverFactory<SolutionResults> getFactory();

    public abstract TurbineSpecification getSpecs();

    public abstract ForecastHorizonErrorDistribution getDistribution();

    public abstract ImbalancePriceInputData getImbalIn();

    public static ExperimentParams create(WindBasedInputData inputData,
            AbstractSolverFactory<SolutionResults> factory, TurbineSpecification specs,
            ForecastHorizonErrorDistribution distribution, ImbalancePriceInputData imbalIn) {
        return new AutoValue_WgmfGameParams(inputData, factory, specs, distribution, imbalIn);
    }
}
