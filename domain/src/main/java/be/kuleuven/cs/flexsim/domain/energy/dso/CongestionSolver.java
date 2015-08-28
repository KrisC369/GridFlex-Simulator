package be.kuleuven.cs.flexsim.domain.energy.dso;

import java.util.List;

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
        incrementTick();
    }

    @Override
    public void tick(int t) {
        doTick();
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

    private class DSMCNPInitiator extends CNPInitiator<DSMProposal> {

        @Override
        protected void signalNoSolutionFound() {
            // TODO Auto-generated method stub
        }

        @Override
        public DSMProposal findBestProposal(List<DSMProposal> props, DSMProposal description) {
            return CollectionUtils.argMax(props, new IntNNFunction<DSMProposal>() {
                @Override
                public int apply(DSMProposal input) {
                    double sum = 0;
                    for (int i = 0; i < DSM_ALLOCATION_DURATION; i++) {
                        sum += getCongestion().value(i) - input.getTargetValue();
                    }
                    return (int) (sum * 100);
                }
            });
        }

        @Override
        public DSMProposal getWorkUnitDescription() {
            double cong = getCongestion().value(getTick());
            return DSMProposal.create("CNP for activation for tick: " + getTick(), cong, getTick(),
                    getTick() + DSM_ALLOCATION_DURATION);
        }

    }

}
