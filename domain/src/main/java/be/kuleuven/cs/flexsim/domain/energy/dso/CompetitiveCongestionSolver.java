package be.kuleuven.cs.flexsim.domain.energy.dso;

import be.kuleuven.cs.flexsim.domain.util.CollectionUtils;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.domain.util.IntNNFunction;
import be.kuleuven.cs.flexsim.protocol.contractnet.CNPInitiator;

import java.util.List;
import java.util.Optional;

/**
 * Entity that solves congestion on local distribution grids by contracting DSM
 * partners and other solutions.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class CompetitiveCongestionSolver extends AbstractCongestionSolver {
    private CNPInitiator<DSMProposal> solverInstance;
    private final IntNNFunction<DSMProposal> usefullnessFunction = input -> {
        double theoreticalMax = DSM_ALLOCATION_DURATION
                * (input.getTargetValue() / 4.0);
        // higher power rate is higher score
        return (int) (theoreticalMax * 1000);
    };

    /**
     * Default constructor.
     * 
     * @param profile
     *            The congestion profile to solve.
     * @param forecastHorizon
     *            The forecast horizon.
     */
    public CompetitiveCongestionSolver(CongestionProfile profile,
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
    public CompetitiveCongestionSolver(CongestionProfile profile,
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

        private Optional<DSMProposal> secondBest = java.util.Optional
                .empty();

        @Override
        protected void signalNoSolutionFound() {
            // NOOP
        }

        @Override
        public Optional<DSMProposal> findBestProposal(List<DSMProposal> props,
                DSMProposal description) {
            secondBest = java.util.Optional.empty();
            if (props.isEmpty()) {
                return Optional.empty();
            }
            final DSMProposal max = CollectionUtils.argMax(props,
                    usefullnessFunction);
            props.remove(max);
            if (!props.isEmpty()) {
                secondBest = java.util.Optional.ofNullable(
                        CollectionUtils.argMax(props, usefullnessFunction));

            }
            return Optional.of(max);
        }

        @Override
        public java.util.Optional<DSMProposal> getWorkUnitDescription() {
            return getWorkProposal();
        }

        @Override
        public void notifyWorkDone(DSMProposal prop) {
            // noop
        }

        @Override
        public DSMProposal updateWorkDescription(DSMProposal best) {
            if (secondBest.isPresent() && best.getTargetValue() < secondBest
                    .get().getTargetValue()) {
                throw new IllegalStateException(
                        "Some weird shit happened. Secondbest with higher activation? at tick "
                                + best.getBeginMark().get());
            }
            if (secondBest.isPresent() && best.getTargetValue() > secondBest
                    .get().getTargetValue()) {
                return DSMProposal.create(best.getDescription(),
                        secondBest.get().getTargetValue(), best.getValuation(),
                        best.getBeginMark().get(), best.getEndMark().get());
            }
            return best;
        }

    }
}
