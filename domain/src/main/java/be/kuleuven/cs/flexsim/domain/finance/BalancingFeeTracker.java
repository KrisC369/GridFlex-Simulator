/**
 *
 */
package be.kuleuven.cs.flexsim.domain.finance;

import be.kuleuven.cs.flexsim.domain.site.Site;
import org.slf4j.LoggerFactory;

/**
 * A tracker that can observe flex activations and value them economically.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
class BalancingFeeTracker extends FinanceTrackerImpl {

    private final int fixedActivationFee;
    private int activationCount;
    private final Site target;
    private final double retributionFactor;

    /**
     * Default constructor.
     *
     * @param s      The site to attach to.
     * @param reward The reward for an activation.
     */
    BalancingFeeTracker(final Site s, final int reward) {
        this(s, reward, 0);
    }

    /**
     * Constructor with specifiable retribution factor.
     *
     * @param s      The site to attach to.
     * @param reward The reward for an activation.
     * @param factor The retribution factor.
     */
    BalancingFeeTracker(final Site s, final int reward, final double factor) {
        super(s, RewardModel.NONE, DebtModel.NONE);
        this.retributionFactor = 1 - factor;
        this.fixedActivationFee = reward;
        this.activationCount = 0;
        this.target = s;
        s.addActivationListener(arg -> {
            final double t = arg.getT();
            increaseTotalReward((int) (fixedActivationFee * retributionFactor
                    * arg.getDeltaP() * (t > 0 ? t : 1.0)));
            incrementCount();
            logCount();
        });
    }

    private void logCount() {
        LoggerFactory.getLogger(BalancingFeeTracker.class).debug(
                "So far, {} activations have been logged for target: {}",
                activationCount, target);
    }

    private void incrementCount() {
        this.activationCount++;
    }
}
