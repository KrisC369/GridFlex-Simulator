package be.kuleuven.cs.flexsim.domain.energy.dso;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

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
public class CompetitiveCongestionSolver extends AbstractCongestionSolver {
    private CNPInitiator<DSMProposal> solverInstance;
    private final IntNNFunction<DSMProposal> usefullnessFunction = new IntNNFunction<DSMProposal>() {
        @Override
        public int apply(DSMProposal input) {
            // double sum = 0;
            // for (int i = 0; i < FastMath.min(DSM_ALLOCATION_DURATION,
            // getModifiableProfileAfterDSM().length() - getTick()
            // - 1); i++) {
            // sum += FastMath.min(
            // getModifiableProfileAfterDSM().value(getTick() + i),
            // (input.getTargetValue() / 4.0));
            // }
            double theoreticalMax = DSM_ALLOCATION_DURATION
                    * (input.getTargetValue() / 4.0);
                    // double relativeSucc = sum / theoreticalMax;

            // return (int) (relativeSucc * input.getValuation() * 1000);
            return (int) (theoreticalMax * 1000); // higher
                                                  // power
                                                  // rate
                                                  // is
                                                  // higher
                                                  // score.
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

        private Optional<DSMProposal> secondBest = Optional.absent();

        @Override
        protected void signalNoSolutionFound() {
            // TODO Auto-generated method stub
        }

        @Override
        public @Nullable DSMProposal findBestProposal(List<DSMProposal> props,
                DSMProposal description) {

            // if (description.getBeginMark().get() == 2513) {
            // System.out.println("test");
            // }
            secondBest = Optional.absent();

            if (props.isEmpty()) {
                return null;
            }
            final DSMProposal max = CollectionUtils.argMax(props,
                    usefullnessFunction);
            props.remove(max);
            if (!props.isEmpty()) {
                secondBest = Optional.fromNullable(
                        CollectionUtils.argMax(props, usefullnessFunction));

            }
            return max;
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
            if (secondBest.isPresent() && best.getTargetValue() < secondBest
                    .get().getTargetValue()) {
                throw new IllegalStateException(
                        "Some weird shit happened. Secondbest with higher activation? at tick "
                                + best.getBeginMark().get());
            }
            if (secondBest.isPresent() && best.getTargetValue() > secondBest
                    .get().getTargetValue()) {
                // System.out.println("SetLow");
                return DSMProposal.create(best.getDescription(),
                        secondBest.get().getTargetValue(), best.getValuation(),
                        best.getBeginMark().get(), best.getEndMark().get());
            }
            return best;
        }

    }

    private abstract class MyPredicate<T> implements Predicate<T> {

        @Override
        public abstract boolean apply(@Nullable T input);

        @Override
        public boolean equals(@Nullable Object obj) {
            return super.equals(obj);
        }
    }
}
