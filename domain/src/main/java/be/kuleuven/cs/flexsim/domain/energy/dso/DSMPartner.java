package be.kuleuven.cs.flexsim.domain.energy.dso;

import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * DSMPartners represent industrial companies offering power consumption
 * flexibility. The flexibility wanted is mainly the increase in power
 * consumption to decrease congestion on distribution networks caused by RES.
 * 
 * The specs for this DSM participant are similar to the specs of the R3DP
 * product by Elia. R3DP specifies curtailment in stead of increased power
 * consumption, though.
 * 
 * The time scale discretization for this DSM partner is set at 15 minutes.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class DSMPartner implements SimulationComponent {
    /**
     * The maximum amount of activations according to R3DP specs is 40/year.
     */
    public static final int R3DPMAX_ACTIVATIONS = 40;
    /**
     * The time between two consecutive activations according to R3DP specs is
     * 12 hours.
     */
    public static final int INTERACTIVATION_TIME = 4 * 12;
    /**
     * The activation duration according to R3DP specs is (max) 2 hours.
     */
    public static final int ACTIVATION_DURATION = 4 * 2;
    private final int maxActivations;
    private final int interactivationTime;
    private final int activationDuration;
    private final int flexPowerRate;

    /**
     * Default constructor according to r3dp specs.
     * 
     * @param powerRate
     *            The amount of instantaneous power this partners is able to
     *            increase during activations.
     */
    public DSMPartner(int powerRate) {
        this.maxActivations = R3DPMAX_ACTIVATIONS;
        this.interactivationTime = INTERACTIVATION_TIME;
        this.activationDuration = ACTIVATION_DURATION;
        this.flexPowerRate = powerRate;
    }

    private void moveHorizons(int t) {

    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public void afterTick(int t) {
    }

    @Override
    public void tick(int t) {
        moveHorizons(t);
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    /**
     * @return the maxActivations
     */
    public final int getMaxActivations() {
        return maxActivations;
    }

    /**
     * @return the interactivationTime
     */
    public final int getInteractivationTime() {
        return interactivationTime;
    }

    /**
     * @return the activationDuration
     */
    public final int getActivationDuration() {
        return activationDuration;
    }

    /**
     * @return the flexPowerRate
     */
    public final int getFlexPowerRate() {
        return flexPowerRate;
    }

}
