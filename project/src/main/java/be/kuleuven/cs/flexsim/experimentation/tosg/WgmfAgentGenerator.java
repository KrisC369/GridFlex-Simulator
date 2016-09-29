package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gametheory.AgentGenerator;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;

/**
 * Configurator for who-gets-my-flex-game.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfAgentGenerator implements
                              AgentGenerator<FlexibilityProvider> {
    private static final double R3DP_GAMMA_SCALE = 677.926;
    private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private final GammaDistribution gd;
    private long currentSeed = 0;

    public WgmfAgentGenerator(long seed) {
        this.currentSeed = seed;
        this.gd = new GammaDistribution(new MersenneTwister(currentSeed), R3DP_GAMMA_SHAPE,
                R3DP_GAMMA_SCALE);
    }

    @Override
    public FlexibilityProvider getAgent() {
        return new FlexProvider(gd.sample());
    }
}
