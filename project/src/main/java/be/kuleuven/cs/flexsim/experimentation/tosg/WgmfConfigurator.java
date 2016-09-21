package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.FlexibilityUtiliser;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.WindErrorGenerator;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.ForecastHorizonErrorDistribution;
import be.kuleuven.cs.flexsim.experimentation.tosg.poc.WindBasedInputData;
import be.kuleuven.cs.gametheory.GameConfigurator;
import be.kuleuven.cs.gametheory.GameInstance;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;

/**
 * Configurator for who-gets-my-flex-game.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfConfigurator implements
                              GameConfigurator<FlexibilityProvider, FlexibilityUtiliser> {
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final long SEED = 1312421L;
    public static final int NUMBER_OF_ACTIONS = 2;
    private final TurbineSpecification specs;
    private final ForecastHorizonErrorDistribution distribution;
    private final GammaDistribution gd;
    private final WindBasedInputData dataIn;
    private final AbstractSolverFactory<SolutionResults> factory;
    private int currentSeed = 0;

    public WgmfConfigurator(WindBasedInputData inputData,
            AbstractSolverFactory<SolutionResults> factory, TurbineSpecification specs,
            ForecastHorizonErrorDistribution distribution) {
        this.gd = new GammaDistribution(new MersenneTwister(SEED + currentSeed++), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
        this.dataIn = inputData;
        this.specs = specs;
        this.distribution = distribution;
        this.factory = factory;
    }

    @Override
    public FlexibilityProvider getAgent() {
        return new FlexProvider(gd.sample());
    }

    @Override
    public GameInstance<FlexibilityProvider, FlexibilityUtiliser> generateInstance() {
        return new WhoGetsMyFlexGame(dataIn, specs,
                new WindErrorGenerator(SEED + currentSeed++, distribution), factory);
    }

    @Override
    public int getActionSpaceSize() {
        return NUMBER_OF_ACTIONS;
    }
}
