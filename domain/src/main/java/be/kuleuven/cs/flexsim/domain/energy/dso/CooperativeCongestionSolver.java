package be.kuleuven.cs.flexsim.domain.energy.dso;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

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
    private int RELATIVE_MAX_VALUE_PERCENT;
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

        @Override
        protected void signalNoSolutionFound() {
            // TODO Auto-generated method stub
        }

        @Override
        public @Nullable DSMProposal findBestProposal(List<DSMProposal> props,
                DSMProposal description) {

            List<DSMProposal> filtered = Lists.newArrayList(
                    Collections2.filter(props, new MyPredicate<DSMProposal>() {
                        @Override
                        public boolean apply(@Nullable DSMProposal input) {
                            if (input == null) {
                                return false;
                            }
                            return filterFunction
                                    .apply(input) <= RELATIVE_MAX_VALUE_PERCENT
                                            ? true : false;
                        }
                    }));
            if (filtered.isEmpty()) {
                return null;
            }
            return CollectionUtils.argMax(filtered, choiceFunction);
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
