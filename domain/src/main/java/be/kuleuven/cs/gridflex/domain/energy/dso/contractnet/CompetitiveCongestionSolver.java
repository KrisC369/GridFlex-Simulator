package be.kuleuven.cs.gridflex.domain.energy.dso.contractnet;

import be.kuleuven.cs.gridflex.domain.util.CollectionUtils;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.AbstractTimeSeriesImplementation;
import be.kuleuven.cs.gridflex.protocol.contractnet.ContractNetInitiator;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

/**
 * Entity that solves congestion on local distribution grids by contracting DSM
 * partners and other solutions.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class CompetitiveCongestionSolver extends AbstractCongestionSolver {
    private final ContractNetInitiator<DSMProposal> solverInstance;
    private final ToIntFunction<DSMProposal> usefullnessFunction = input -> {
        double theoreticalMax = DSM_ALLOCATION_DURATION
                * (input.getTargetValue() / 4.0);
        // higher power rate is higher score
        return (int) (theoreticalMax * 1000);
    };

    /**
     * Default constructor.
     *
     * @param profile         The congestion profile to solve.
     * @param forecastHorizon The forecast horizon.
     */
    public CompetitiveCongestionSolver(final AbstractTimeSeriesImplementation profile,
            final int forecastHorizon) {
        this(profile, forecastHorizon, 0);
    }

    /**
     * Default constructor.
     *
     * @param profile          The congestion profile to solve.
     * @param forecastHorizon  The forecast horizon.
     * @param maxRelativeValue The maximum relative congestion resolve value.
     */
    public CompetitiveCongestionSolver(final AbstractTimeSeriesImplementation profile,
            final int forecastHorizon, final int maxRelativeValue) {
        super(profile, forecastHorizon, maxRelativeValue);
        this.solverInstance = new DSMContractNetInitiator();
    }

    /**
     * @return the solverInstance
     */
    @Override
    protected ContractNetInitiator<DSMProposal> getSolverInstance() {
        return this.solverInstance;
    }

    private class DSMContractNetInitiator extends ContractNetInitiator<DSMProposal> {

        private Optional<DSMProposal> secondBest = Optional.empty();

        @Override
        protected void signalNoSolutionFound() {
            // NOOP
        }

        @Override
        public Optional<DSMProposal> findBestProposal(final List<DSMProposal> props,
                final DSMProposal description) {
            secondBest = Optional.empty();
            if (props.isEmpty()) {
                return Optional.empty();
            }
            final DSMProposal max = CollectionUtils.argMax(props,
                    usefullnessFunction);
            props.remove(max);
            if (!props.isEmpty()) {
                secondBest = Optional.ofNullable(
                        CollectionUtils.argMax(props, usefullnessFunction));

            }
            return Optional.of(max);
        }

        @Override
        public Optional<DSMProposal> getWorkUnitDescription() {
            return getWorkProposal();
        }

        @Override
        public void notifyWorkDone(final DSMProposal prop) {
            // noop
        }

        @Override
        public DSMProposal updateWorkDescription(final DSMProposal best) {
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
