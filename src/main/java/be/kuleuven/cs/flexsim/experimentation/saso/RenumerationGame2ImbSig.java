package be.kuleuven.cs.flexsim.experimentation.saso;

import org.apache.commons.math3.random.MersenneTwister;

import be.kuleuven.cs.flexsim.domain.aggregation.brp.BRPAggregator;
import be.kuleuven.cs.flexsim.domain.aggregation.brp.PriceSignal;
import be.kuleuven.cs.flexsim.domain.energy.tso.random.RandomTSO;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTracker;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.experimentation.AggregationGame;

/**
 * Represents a game with two possible actions to choose between.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class RenumerationGame2ImbSig extends
        AggregationGame<Site, BRPAggregator> {

    private static final int ACTIONSPACE_SIZE = 2;

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed
     *            The seed for this experiment.
     * @param baselineConsumption
     *            The baseline for the sites consumption. This is used to base
     *            production params on.
     * @param factor1
     *            The retribution factor for agent 1.
     * @param factor2
     *            The retribution factor for agent 2
     */
    public RenumerationGame2ImbSig(int seed, int baselineConsumption,
            double factor1, double factor2) {
        super(seed);

        RandomTSO tso1 = new RandomTSO(-200, 200, new MersenneTwister(seed));
        RandomTSO tso2 = new RandomTSO(-100, 100, new MersenneTwister(seed));
        addSimComponent(tso1);
        addSimComponent(tso2);
        this.addAggregator(new BRPAggregator(tso1, new PriceSignal() {

            @Override
            public int getCurrentPrice() {
                return 100;
            }
        }, factor1, 1 - factor1));
        this.addAggregator(new BRPAggregator(tso2, new PriceSignal() {

            @Override
            public int getCurrentPrice() {
                return 100;
            }
        }, factor2, 1 - factor2));
    }

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed
     *            The seed for this experiment.
     * @param baselineConsumption
     *            The baseline for the sites consumption. This is used to base
     *            production params on.
     */
    public RenumerationGame2ImbSig(int seed, int baselineConsumption) {
        this(seed, baselineConsumption, 1, 1);

    }

    @Override
    public void fixActionToAgent(Site agent, BRPAggregator action) {
        addSite(agent);
        addChoice(agent, action);
        FinanceTracker fti;
        action.registerClient(agent);
        fti = action.getFinanceTrackerFor(agent);
        addFinanceTracker(fti);
    }

    /**
     * @return the actionspacesize
     */
    public static final int getActionspacesize() {
        return ACTIONSPACE_SIZE;
    }
}
