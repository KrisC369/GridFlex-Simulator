package be.kuleuven.cs.flexsim.domain.energy.dso;

import be.kuleuven.cs.flexsim.protocol.Responder;
import be.kuleuven.cs.flexsim.protocol.contractnet.CNPResponder;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import org.apache.commons.math3.util.FastMath;

import java.util.Collections;
import java.util.List;

/**
 * DSMPartners represent industrial companies offering power consumption
 * flexibility. The flexibility wanted is mainly the increase in power
 * consumption to decrease congestion on distribution networks caused by RES.
 * The specs for this DSM participant are similar to the specs of the R3DP
 * product by Elia. R3DP specifies curtailment in stead of increased power
 * consumption, though. The time scale discretization for this DSM partner is
 * set at 15 minutes.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
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
    private static final int OPERATING_TIME_LIMIT = 4 * 24 * 365;
    private static final double CURRENT_ALLOWED_DEVIATION = 0.10;

    private final int maxActivations;
    private final int interactivationTime;
    private final int activationDuration;
    private final int flexPowerRate;
    private final CNPResponder<DSMProposal> dsmAPI;
    private final double[] activationMarker;
    private int currentActivations;
    private double currentAllowedDeviation;

    /**
     * Default constructor according to r3dp specs.
     *
     * @param powerRate The amount of instantaneous power this partners is able to
     *                  increase during activations.
     */
    public DSMPartner(int powerRate) {
        this(R3DPMAX_ACTIVATIONS, INTERACTIVATION_TIME, ACTIVATION_DURATION,
                powerRate, CURRENT_ALLOWED_DEVIATION);
    }

    /**
     * Default constructor according to r3dp specs.
     *
     * @param powerRate The amount of instantaneous power this partners is able to
     *                  increase during activations.
     * @param deviation The allowed relative deviation to the mean goal number of
     *                  activations so far.
     */
    public DSMPartner(int powerRate, double deviation) {
        this(R3DPMAX_ACTIVATIONS, INTERACTIVATION_TIME, ACTIVATION_DURATION,
                powerRate, deviation);
    }

    /**
     * Full argument constructor.
     *
     * @param maxActivations
     * @param interactivationTime
     * @param activationDuration
     * @param flexPowerRate       The amount of instantaneous power this partners is able to
     *                            increase during activations.
     * @param deviation           The allowed relative deviation to the mean goal number of
     *                            activations so far.
     */
    DSMPartner(int maxActivations, int interactivationTime,
            int activationDuration, int flexPowerRate, double deviation) {
        this.maxActivations = maxActivations;
        this.interactivationTime = interactivationTime;
        this.activationDuration = activationDuration;
        this.flexPowerRate = flexPowerRate;
        this.dsmAPI = new DSMCNPResponder();
        this.activationMarker = new double[OPERATING_TIME_LIMIT];
        this.currentActivations = 0;
        this.currentAllowedDeviation = deviation;
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public void afterTick(int t) {
    }

    @Override
    public void tick(int t) {
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

    /**
     * @return Returns the dsm communication api for the role of responder in
     * this dsm partner.
     */
    public Responder<DSMProposal> getDSMAPI() {
        return this.dsmAPI;
    }

    /**
     * Returns the amount of power consumption increased at the specified time
     * step.
     *
     * @param timeMark The mark to check.
     * @return the power increase amount.
     */
    public double getCurtailment(int timeMark) {
        return activationMarker[timeMark];
    }

    /**
     * @return the current number of activations planned or triggered.
     */
    public final int getCurrentActivations() {
        return this.currentActivations;
    }

    /**
     * Mark activation
     *
     * @param begin BeginMark
     * @param end   EndMark
     */
    private void markActivation(Integer begin, Integer end,
            double targetPowerRate) {
        for (int i = 0; i < end - begin; i++) {
            activationMarker[begin + i] = targetPowerRate;
        }
        incrementActivations();
    }

    private void incrementActivations() {
        this.currentActivations += 1;
    }

    private boolean canActivateDuring(Integer begin, Integer end) {
        if (getValuation(begin) >= currentAllowedDeviation) {
            return false;
        }
        if (begin < 0
                || end + getActivationDuration() >= OPERATING_TIME_LIMIT) {
            return false;
        }

        if (getCurrentActivations() >= getMaxActivations()) {
            return false;
        }
        for (int i = begin; i < end; i++) {
            if (activationMarker[i] > 0) {
                return false;
            }
        }
        for (int i = FastMath.max(0,
                begin - getInteractivationTime()); i < begin; i++) {
            if (activationMarker[i] > 0) {
                return false;
            }
        }
        for (int i = begin + getActivationDuration(); i < FastMath.min(
                begin + getActivationDuration() + getInteractivationTime(),
                OPERATING_TIME_LIMIT); i++) {
            if (activationMarker[i] > 0) {
                return false;
            }
        }
        return true;
    }

    private double getValuation(DSMProposal prop) {
        return getValuation(prop.getBeginMark().get());
    }

    private double getValuation(int beginMark) {
        double factor = maxActivations / (double) OPERATING_TIME_LIMIT;
        double goal = beginMark * factor;
        return (currentActivations - goal) / maxActivations;
    }

    private class DSMCNPResponder extends CNPResponder<DSMProposal> {
        @Override
        protected DSMProposal makeProposalForCNP(DSMProposal arg)
                throws CanNotFindProposalException {
            if (canActivateDuring(arg.getBeginMark().get(),
                    arg.getEndMark().get())) {
                StringBuilder b = new StringBuilder("Proposal from ")
                        .append(this.toString()).append(" with ")
                        .append(getFlexPowerRate()).append("kW");
                return DSMProposal.create(b.toString(), getFlexPowerRate(),
                        getValuation(arg), arg.getBeginMark().get(),
                        arg.getEndMark().get());
            }
            throw new CanNotFindProposalException();
        }

        @Override
        protected boolean performWorkUnitFor(DSMProposal arg) {
            markActivation(arg.getBeginMark().get(), arg.getEndMark().get(),
                    arg.getTargetValue());
            return true;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DSMPartner [flexPowerRate=").append(flexPowerRate)
                .append(", currentActivations=").append(currentActivations)
                .append("]");
        return builder.toString();
    }

}
