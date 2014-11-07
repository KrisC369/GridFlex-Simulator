/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.finance;

import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.site.ActivateFlexCommand;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;

/**
 * A tracker that can observe flex activations and value them economically.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
class BalancingFeeTracker extends FinanceTrackerImpl {

    private final int FIXED_ACTIVATION_FEE;
    private int activationCount;
    private Site target;

    /**
     * Default constructor.
     * 
     * @param s
     *            The site to attach to.
     */
    BalancingFeeTracker(Site s) {
        super(s, RewardModel.CONSTANT, DebtModel.CONSTANT);
        this.FIXED_ACTIVATION_FEE = 300000;
        this.activationCount = 0;
        this.target = s;
        s.addActivationListener(new Listener<ActivateFlexCommand>() {
            @Override
            public void eventOccurred(ActivateFlexCommand arg) {
                increaseTotalReward(FIXED_ACTIVATION_FEE);
                incrementCount();
                logCount();
            }

        });
    }

    private void logCount() {
        LoggerFactory.getLogger(FinanceTracker.class).debug(
                "So far, {} activations have been logged for target: {}",
                activationCount, target);
    }

    private void incrementCount() {
        this.activationCount++;
    }
}
