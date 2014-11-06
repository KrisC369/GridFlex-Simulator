/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.finance;

import be.kuleuven.cs.flexsim.domain.site.ActivateFlexCommand;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class BalancingFeeTracker extends FinanceTrackerImpl {

    private final int fixedActivationFee;

    /**
     * 
     * @param s
     */
    public BalancingFeeTracker(Site s) {
        super(s, RewardModel.CONSTANT, DebtModel.CONSTANT);
        this.fixedActivationFee = 30;
        s.addActivationListener(new Listener<ActivateFlexCommand>() {
            @Override
            public void eventOccurred(ActivateFlexCommand arg) {
                increaseTotalReward(30);
            }
        });
    }
}
