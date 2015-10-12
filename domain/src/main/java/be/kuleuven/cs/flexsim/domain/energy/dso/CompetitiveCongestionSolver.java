package be.kuleuven.cs.flexsim.domain.energy.dso;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import autovalue.shaded.com.google.common.common.base.Optional;
import be.kuleuven.cs.flexsim.domain.util.CollectionUtils;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.domain.util.IntNNFunction;
import be.kuleuven.cs.flexsim.domain.util.MathUtils;
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
    private final IntNNFunction<DSMProposal> valueFunction = new IntNNFunction<DSMProposal>() {
        @Override
        public int apply(DSMProposal input) {
            // TODO maximize efficiency also.
            // This maximizes to no-activation.
            // TODO take responder valuation in account
            // double sum = 0;
            double max = 0;
            for (int i = 0; i < FastMath.min(DSM_ALLOCATION_DURATION,
                    getModifiableProfileAfterDSM().length() - getTick()
                            - 1); i++) { // TODO
                // check
                // bounds.
                double res = getModifiableProfileAfterDSM().value(getTick() + i)
                        - (input.getTargetValue() / 4.0);
                // sum += res < 0 ? res : 0;
                max = res < max ? res : max;
            }
            return (int) ((-max / (input.getTargetValue() / 4.0)) * 100);
        }
    };
    private final IntNNFunction<DSMProposal> usefullnessFunction = new IntNNFunction<DSMProposal>() {
        @Override
        public int apply(DSMProposal input) {
            double sum = 0;
            for (int i = 0; i < FastMath.min(DSM_ALLOCATION_DURATION,
                    getModifiableProfileAfterDSM().length() - getTick()
                            - 1); i++) {
                sum += FastMath.min(
                        getModifiableProfileAfterDSM().value(getTick() + i),
                        (input.getTargetValue() / 4.0));
            }
            double theoreticalMax = DSM_ALLOCATION_DURATION
                    * (input.getTargetValue() / 4.0);
            double relativeSucc = sum / theoreticalMax;

            return (int) (relativeSucc * input.getValuation() * 1000);
        }
    };

    private final IntNNFunction<DSMProposal> filterFunction = new IntNNFunction<DSMProposal>() {
        @Override
        public int apply(DSMProposal input) {
            // TODO maximize efficiency also.
            // This maximizes to no-activation.
            // TODO take responder valuation in account
            // double sum = 0;
            double max = 0;
            for (int i = 0; i < FastMath.min(DSM_ALLOCATION_DURATION,
                    getModifiableProfileAfterDSM().length() - getTick()
                            - 1); i++) { // TODO
                // check
                // bounds.
                double res = getModifiableProfileAfterDSM().value(getTick() + i)
                        - (input.getTargetValue() / 4.0);
                // sum += res < 0 ? res : 0;
                max = res < max ? res : max;
            }
            return (int) ((-max / (input.getTargetValue() / 4.0)) * 100);
        }
    };
    private final IntNNFunction<DSMProposal> choiceFunction = new IntNNFunction<DSMProposal>() {
        @Override
        public int apply(DSMProposal input) {
            double remainingCong = 0;
            for (int i = 0; i < FastMath.min(DSM_ALLOCATION_DURATION - 1,
                    getModifiableProfileAfterDSM().length() - getTick() - 1
                            - 1); i++) {
                double n1 = getModifiableProfileAfterDSM().value(getTick() + i)
                        - (input.getTargetValue() / 4.0);
                double n2 = getModifiableProfileAfterDSM().value(
                        getTick() + i + 1) - (input.getTargetValue() / 4.0);
                remainingCong += MathUtils.trapzPos(n1, n2, 1);

                // sum += FastMath.min(
                // getModifiableProfileAfterDSM().value(getTick() + i),
                // (input.getTargetValue() / 4.0));
            }
            double theoreticalMax = DSM_ALLOCATION_DURATION
                    * (input.getTargetValue() / 4.0);
            // double relativeSucc = remainingCong / theoreticalMax;

            return (int) (remainingCong * 1000);
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
            secondBest = Optional.absent();

            List<DSMProposal> filtered = Lists.newArrayList(
                    Collections2.filter(props, new MyPredicate<DSMProposal>() {
                        @Override
                        public boolean apply(@Nullable DSMProposal input) {
                            if (input == null) {
                                return false;
                            }
                            return valueFunction
                                    .apply(input) <= RELATIVE_MAX_VALUE_PERCENT
                                            ? true : false;
                        }
                    }));
            if (filtered.isEmpty()) {
                return null;
            }
            final DSMProposal max = CollectionUtils.argMax(filtered,
                    usefullnessFunction);
            filtered.remove(max);
            if (!filtered.isEmpty()) {
                secondBest = Optional.fromNullable(
                        CollectionUtils.argMax(filtered, usefullnessFunction));

            }
            return max;
        }

        @Override
        public DSMProposal getWorkUnitDescription() {
            double cong = getCongestion().value(getTick());
            return DSMProposal.create(
                    "CNP for activation for tick: " + getTick(), cong, 0,
                    getTick(), getTick() + DSM_ALLOCATION_DURATION);
        }

        @Override
        public void notifyWorkDone(DSMProposal prop) {
            // noop
        }

        @Override
        public DSMProposal updateWorkDescription(DSMProposal best) {
            if (secondBest.isPresent()) {
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
