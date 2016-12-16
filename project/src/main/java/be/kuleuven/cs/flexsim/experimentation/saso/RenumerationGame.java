package be.kuleuven.cs.flexsim.experimentation.saso;

import be.kuleuven.cs.flexsim.domain.aggregation.brp.AncilServiceNominationManager;
import be.kuleuven.cs.flexsim.domain.aggregation.brp.BRPAggregator;
import be.kuleuven.cs.flexsim.domain.aggregation.brp.Nomination;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.energy.tso.random.RandomTSO;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTracker;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.experimentation.AggregationGame;
import com.google.common.collect.Lists;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.List;

/**
 * Represents a game with two possible actions to choose between.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class RenumerationGame extends AggregationGame<Site, BRPAggregator> {

    private static final int FIXED_PRICE_SIGNAL = 100;
    private static final int FIXED_IMBAL_SIGNAL_WIDTH = 200;

    private static final int ACTIONSPACE_SIZE = 2;
    private final RandomTSO tso;
    private long runningRemainingImbalance;

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
    public RenumerationGame(final int seed, final double factor1, final double factor2) {
        super(seed);
        this.tso = new RandomTSO(-FIXED_IMBAL_SIGNAL_WIDTH,
                FIXED_IMBAL_SIGNAL_WIDTH, new MersenneTwister(seed));
        this.addSimComponent(tso);
        this.runningRemainingImbalance = 0;
        this.addAggregator(new BRPAggregator(tso, () -> FIXED_PRICE_SIGNAL,
                factor1, 1 - factor1));
        this.addAggregator(new BRPAggregator(tso, () -> FIXED_PRICE_SIGNAL,
                factor2, 1 - factor2));
        for (final BRPAggregator agg : getActionSet()) {
            agg.registerNominationManager(new EfficiencyTracker());
        }
    }

    /**
     * Default constructor for this game with two actions.
     *
     * @param seed
     *            The seed for this experiment.
     */
    public RenumerationGame(final int seed) {
        this(seed, 1, 1);

    }

    @Override
    public void fixActionToAgent(final Site agent, final BRPAggregator action) {
        this.addSite(agent);
        addChoice(agent, action);
        final FinanceTracker fti;
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

    protected List<BalancingSignal> getBalancingSignals() {
        return Lists.newArrayList((BalancingSignal) tso);
    }

    @Override
    public double getExternalityValue() {
        return runningRemainingImbalance;
    }

    protected class EfficiencyTracker implements AncilServiceNominationManager {

        @Override
        public void registerNomination(final Nomination target) {
            final long remainingImb = target.getTargetImbalanceVolume()
                    - target.getRemediedImbalanceVolume();
            runningRemainingImbalance += remainingImb;
        }
    }
}
