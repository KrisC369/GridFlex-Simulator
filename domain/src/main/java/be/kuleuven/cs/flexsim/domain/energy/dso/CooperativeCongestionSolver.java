package be.kuleuven.cs.flexsim.domain.energy.dso;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Optional;

import be.kuleuven.cs.flexsim.domain.util.CollectionUtils;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.domain.util.IntNNFunction;
import be.kuleuven.cs.flexsim.protocol.contractnet.CNPInitiator;

/**
 * Entity that solves congestion on local distribution grids by contracting DSM
 * partners and other solutions.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class CooperativeCongestionSolver extends AbstractCongestionSolver {
    private CNPInitiator<DSMProposal> solverInstance;
    private final IntNNFunction<DSMProposal> choiceFunction = new IntNNFunction<DSMProposal>() {
        @Override
        public int apply(DSMProposal input) {
            double sum = 0;
            for (int i = 0; i < FastMath.min(DSM_ALLOCATION_DURATION,
                    getModifiableProfileAfterDSM().length() - getTick()
                            - 1); i++) {
                sum += FastMath.min(getHorizon().getDouble(i),
                        (input.getTargetValue() / 4.0));
            }
            // Closest match to congestion is chosen. ties in favor of smaller
            // bids.
            return (int) ((sum * 100000) - input.getTargetValue());
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
    public CooperativeCongestionSolver(CongestionProfile profile,
            int forecastHorizon) {
        this(profile, forecastHorizon, 0);
    }

    /**
     * Default constructor.
     * 
     * @param profile
     *            The congestion profile to solve.
     * @param forecastHorizon
     *            The forecast horizon.
     * @param maxRelativeValue
     *            The maximum relative congestion resolve value.
     */
    public CooperativeCongestionSolver(CongestionProfile profile,
            int forecastHorizon, int maxRelativeValue) {
        super(profile, forecastHorizon, maxRelativeValue);
        this.solverInstance = new DSMCNPInitiator();
    }

    /**
     * @return the solverInstance
     */
    @Override
    protected CNPInitiator<DSMProposal> getSolverInstance() {
        return this.solverInstance;
    }

    private class DSMCNPInitiator extends CNPInitiator<DSMProposal> {

        @Override
        protected void signalNoSolutionFound() {
            // NOOP
        }

        @Override
        public @Nullable DSMProposal findBestProposal(List<DSMProposal> props,
                DSMProposal description) {

            if (props.isEmpty()) {
                return null;
            }
            return CollectionUtils.argMax(props, choiceFunction);
        }

        @Override
        public Optional<DSMProposal> getWorkUnitDescription() {
            return getWorkProposal();
        }

        @Override
        public void notifyWorkDone(DSMProposal prop) {
            // noop
        }

        @Override
        public DSMProposal updateWorkDescription(DSMProposal best) {
            return best;
        }

    }
}
