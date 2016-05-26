package be.kuleuven.cs.flexsim.experimentation.saso;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.flexsim.domain.aggregation.brp.BRPAggregator;
import be.kuleuven.cs.flexsim.domain.energy.tso.random.RandomTSO;

/**
 * Represents a game with two possible actions to choose between.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class RenumerationGame2ImbSig extends RenumerationGame {

    private static final int FIXED_PRICE_SIGNAL = 100;
    private static final int FIXED_IMBAL_SIGNAL_WIDTH = 200;

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed
     *            The seed for this experiment.
     * @param factor1
     *            The retribution factor for agent 1.
     * @param factor2
     *            The retribution factor for agent 2
     */
    public RenumerationGame2ImbSig(int seed, double factor1, double factor2) {
        super(seed);

        RandomTSO tso1 = new RandomTSO(-FIXED_IMBAL_SIGNAL_WIDTH,
                FIXED_IMBAL_SIGNAL_WIDTH, new MersenneTwister(seed));
        RandomTSO tso2 = new RandomTSO(-FIXED_IMBAL_SIGNAL_WIDTH / 2,
                FIXED_IMBAL_SIGNAL_WIDTH / 2, new MersenneTwister(seed));
        addSimComponent(tso1);
        addSimComponent(tso2);
        this.addAggregator(new BRPAggregator(tso1, () -> FIXED_PRICE_SIGNAL,
                factor1, 1 - factor1));
        this.addAggregator(new BRPAggregator(tso2, () -> FIXED_PRICE_SIGNAL,
                factor2, 1 - factor2));
        for (BRPAggregator agg : getActionSet()) {
            agg.registerNominationManager(new EfficiencyTracker());
        }
    }

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed
     *            The seed for this experiment.
     */
    public RenumerationGame2ImbSig(int seed) {
        this(seed, 1, 1);

    }

}
