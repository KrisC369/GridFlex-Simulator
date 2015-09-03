package be.kuleuven.cs.flexsim.domain.energy.dso;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.util.FastMath;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.domain.util.CollectionUtils;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.domain.util.IntNNFunction;
import be.kuleuven.cs.flexsim.protocol.contractnet.CNPInitiator;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

/**
 * Entity that solves congestion on local distribution grids by contracting DSM
 * partners and other solutions.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class CongestionSolver implements SimulationComponent {
    private final static int DSM_ALLOCATION_DURATION = 4 * 2;
    private final CongestionProfile congestion;
    private final List<DSMPartner> dsms;
    private int tick;
    private final int forecastHorizon;
    private CNPInitiator<DSMProposal> solverInstance;
    private BigDecimal remediedCongestionCount;
    private final CongestionProfile afterDSMprofile;
    private final IntNNFunction<DSMProposal> valueFunction = new IntNNFunction<DSMProposal>() {
        @Override
        public int apply(DSMProposal input) {
            // TODO maximize efficiency also.
            // This maximizes to no-activation.
            // TODO take responder valuation in account
            double sum = 0;
            for (int i = 0; i < DSM_ALLOCATION_DURATION; i++) {
                sum += afterDSMprofile.value(getTick() + i) - (input.getTargetValue() / 4.0);
            }
            return (int) (sum);
        }
    };
    private final IntNNFunction<DSMProposal> usefullnessFunction = new IntNNFunction<DSMProposal>() {
        @Override
        public int apply(DSMProposal input) {
            double sum = 0;
            for (int i = 0; i < DSM_ALLOCATION_DURATION; i++) {
                sum += FastMath.min(afterDSMprofile.value(getTick() + i), (input.getTargetValue() / 4.0));
            }
            double theoreticalMax = DSM_ALLOCATION_DURATION * (input.getTargetValue() / 4);
            double relativeSucc = sum / theoreticalMax;

            return (int) (relativeSucc * input.getValuation() * 1000);
        }
    };

    /**
     * Default constructor.
     * 
     * @param profile
     *            The congestion profile to solve.
     * @param forecastHorizon
     *            The forecast horizon.
     */
    public CongestionSolver(CongestionProfile profile, int forecastHorizon) {
        this.congestion = profile;
        this.dsms = Lists.newArrayList();
        this.tick = 0;
        this.solverInstance = new DSMCNPInitiator();
        this.forecastHorizon = forecastHorizon;
        this.remediedCongestionCount = new BigDecimal(0);
        this.afterDSMprofile = CongestionProfile.createFromTimeSeries(profile);
    }

    /**
     * Register this dsm partner to this solver instance.
     * 
     * @param dsm
     *            the partner to add.
     */
    public void registerDSMPartner(DSMPartner dsm) {
        dsms.add(dsm);
        getSolverInstance().registerResponder(dsm.getDSMAPI());
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    @Override
    public void afterTick(int t) {
        getWorkResults();
        incrementTick();
    }

    @Override
    public void tick(int t) {
        doTick();
    }

    private void getWorkResults() {
        for (DSMPartner d : getDsms()) {
            double dsmv = d.getCurtailment(getTick()) / 4.0;
            if (dsmv >= afterDSMprofile.value(getTick())) {
                this.remediedCongestionCount = this.remediedCongestionCount
                        .add(BigDecimal.valueOf(afterDSMprofile.value(getTick())));
            } else {
                this.remediedCongestionCount = this.remediedCongestionCount.add(BigDecimal.valueOf(dsmv));
            }
            this.afterDSMprofile.changeValue(getTick(), afterDSMprofile.value(getTick()) - dsmv);
        }
    }

    private void doTick() {
        getSolverInstance().sollicitWork();
    }

    /**
     * @return the tick
     */
    private final int getTick() {
        return tick;
    }

    /**
     * @param tick
     *            the tick to set
     */
    private final void incrementTick() {
        this.tick = tick + 1;
    }

    /**
     * @return the congestion
     */
    private CongestionProfile getCongestion() {
        return this.congestion;
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return getDsms();
    }

    /**
     * @return the dsms
     */
    public List<DSMPartner> getDsms() {
        return Lists.newArrayList(this.dsms);
    }

    /**
     * @return the solverInstance
     */
    private final CNPInitiator<DSMProposal> getSolverInstance() {
        return this.solverInstance;
    }

    /**
     * @return the forecastHorizon
     */
    public int getForecastHorizon() {
        return this.forecastHorizon;
    }

    /**
     * @return Returns the total remedied congestion so far.
     */
    public double getTotalRemediedCongestion() {
        return this.remediedCongestionCount.doubleValue();
    }

    public CongestionProfile getProfileAfterDSM() {
        return CongestionProfile.createFromTimeSeries(afterDSMprofile);
    }

    private class DSMCNPInitiator extends CNPInitiator<DSMProposal> {

        @Override
        protected void signalNoSolutionFound() {
            // TODO Auto-generated method stub
        }

        @Override
        public @Nullable DSMProposal findBestProposal(List<DSMProposal> props, DSMProposal description) {
            DSMProposal best = CollectionUtils.argMax(props, usefullnessFunction);
            int score = valueFunction.apply(best);
            if (score >= 0 * 8 * 100) {
                return best;
            }
            return null;
        }

        @Override
        public DSMProposal getWorkUnitDescription() {
            double cong = getCongestion().value(getTick());
            return DSMProposal.create("CNP for activation for tick: " + getTick(), cong, 0, getTick(),
                    getTick() + DSM_ALLOCATION_DURATION);
        }

        @Override
        public void notifyWorkDone(DSMProposal prop) {
            // noop
        }

    }

}
