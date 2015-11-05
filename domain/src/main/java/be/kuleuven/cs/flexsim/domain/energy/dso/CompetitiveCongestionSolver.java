package be.kuleuven.cs.flexsim.domain.energy.dso;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.stat.descriptive.rank.Min;

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
    private int RELATIVE_MAX_VALUE_PERCENT;
    // private final IntNNFunction<DSMProposal> valueFunction = new
    // IntNNFunction<DSMProposal>() {
    // @Override
    // public int apply(DSMProposal input) {
    // // TODO maximize efficiency also.
    // // This maximizes to no-activation.
    // // TODO take responder valuation in account
    // // double sum = 0;
    // double max = 0;
    // for (int i = 0; i < FastMath.min(DSM_ALLOCATION_DURATION,
    // getModifiableProfileAfterDSM().length() - getTick()
    // - 1); i++) { // TODO
    // // check
    // // bounds.
    // double res = getModifiableProfileAfterDSM().value(getTick() + i)
    // - (input.getTargetValue() / 4.0);
    // // sum += res < 0 ? res : 0;
    // max = res < max ? res : max;
    // }
    // return (int) ((-max / (input.getTargetValue() / 4.0)) * 100);
    // }
    // };
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

    // private final IntNNFunction<DSMProposal> filterFunction = new
    // IntNNFunction<DSMProposal>() {
    // @Override
    // public int apply(DSMProposal input) {
    // // TODO maximize efficiency also.
    // // This maximizes to no-activation.
    // // TODO take responder valuation in account
    // double sum = 0;
    // // double max = 0;
    // for (int i = 0; i < FastMath.min(DSM_ALLOCATION_DURATION,
    // getModifiableProfileAfterDSM().length() - getTick()
    // - 1); i++) { // TODO
    // // check
    // // bounds.
    // double res = getModifiableProfileAfterDSM().value(getTick() + i)
    // - (input.getTargetValue() / 4.0);
    // sum += res < 0 ? res : 0;
    // // max = res < max ? res : max;
    // }
    // return (int) ((-sum / ((input.getTargetValue() / 4.0) * 8)) * 100);
    // }
    // };
    // private final IntNNFunction<DSMProposal> choiceFunction = new
    // IntNNFunction<DSMProposal>() {
    // @Override
    // public int apply(DSMProposal input) {
    // double remainingCong = 0;
    // for (int i = 0; i < FastMath.min(DSM_ALLOCATION_DURATION - 1,
    // getModifiableProfileAfterDSM().length() - getTick() - 1
    // - 1); i++) {
    // double n1 = getModifiableProfileAfterDSM().value(getTick() + i)
    // - (input.getTargetValue() / 4.0);
    // double n2 = getModifiableProfileAfterDSM().value(
    // getTick() + i + 1) - (input.getTargetValue() / 4.0);
    // remainingCong += MathUtils.trapzPos(n1, n2, 1);
    //
    // // sum += FastMath.min(
    // // getModifiableProfileAfterDSM().value(getTick() + i),
    // // (input.getTargetValue() / 4.0));
    // }
    // double theoreticalMax = DSM_ALLOCATION_DURATION
    // * (input.getTargetValue() / 4.0);
    // // double relativeSucc = remainingCong / theoreticalMax;
    //
    // return (int) (remainingCong * 1000);
    // }
    // };

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
        super(profile, forecastHorizon);
        this.solverInstance = new DSMCNPInitiator();
        this.RELATIVE_MAX_VALUE_PERCENT = maxRelativeValue;
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
            double cong = getCongestion().value(getTick());
            double sum = 0;
            Min m = new Min();
            m.setData(new double[] { getTick() + 8, getCongestion().length() });
            for (int i = getTick(); i < m.evaluate(); i++) {
                sum += getCongestion().value(i);
            }
            if ((sum / (getCongestion().max() * 8.0)
                    * 100) < RELATIVE_MAX_VALUE_PERCENT) {
                return Optional.absent();
            }

            return Optional.fromNullable(DSMProposal.create(
                    "CNP for activation for tick: " + getTick(), cong, 0,
                    getTick(), getTick() + DSM_ALLOCATION_DURATION));
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
