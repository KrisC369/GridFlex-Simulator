package be.kuleuven.cs.flexsim.domain.aggregation.brp;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregationContext;
import be.kuleuven.cs.flexsim.domain.aggregation.independent.IndependentAggregator;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTracker;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;
import be.kuleuven.cs.flexsim.domain.util.CollectionUtils;
import be.kuleuven.cs.flexsim.domain.util.IntNNFunction;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * Represents an BRP-aggregator trying to balance a portfolio.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class BRPAggregator extends IndependentAggregator {

    private final Map<SiteFlexAPI, RenumerationMediator> paymentMapper;
    private final double activationPortion;
    private final double reservePortion;
    private final PriceSignal imbalancePricing;
    private List<AncilServiceNominationManager> nominationManagers;

    /**
     * Default constructor.
     *
     * @param tso
     *            The balancing signal to follow.
     * @param pricing
     *            The pricing signal for the imbalance prices.
     * @param reservation
     *            The reservation payment portion marker.
     * @param activation
     *            The activation payment portion marker.
     */
    public BRPAggregator(BalancingSignal tso, PriceSignal pricing,
            double reservation, double activation) {
        super(tso, 1);
        checkArgument(reservation + activation >= 0
                && reservation + activation <= 1,
                "Reservation and activation should some to x with 0 <= x <= 1");
        this.paymentMapper = Maps.newLinkedHashMap();
        this.activationPortion = activation;
        this.reservePortion = reservation;
        this.imbalancePricing = pricing;
        this.nominationManagers = Lists.newArrayList();
    }

    /**
     * Register a Site to this aggregator. Dispatches to superclass
     * registerClient(SiteFlexAPI client)-method.
     *
     * @param client
     *            The site to register.
     */
    public void registerClient(Site client) {
        super.registerClient(client);
        this.paymentMapper.put(client, createMediator(client));
    }

    private RenumerationMediator createMediator(Site client) {
        return RenumerationMediator.create(client, reservePortion);
    }

    /**
     * Get the financetracker for the SiteFlexAPI in question.
     *
     * @param s
     *            the API reference.
     * @return The registered FinanceTracker.
     */
    public FinanceTracker getFinanceTrackerFor(SiteFlexAPI s) {
        return getActualPaymentMediatorFor(s);
    }

    RenumerationMediator getActualPaymentMediatorFor(SiteFlexAPI s) {
        checkArgument(paymentMapper.containsKey(s),
                "Invalid argument: Site not registered as client.");
        return this.paymentMapper.get(s);
    }

    @Override
    public void tick(int t) {
        // Get target and budget and set budgets.
        int currentImbalVol = getTargetFlex();
        // Make reservation payments
        Multimap<SiteFlexAPI, FlexTuple> flex = gatherFlexInfo();
        int remediedImbalance = doAggregationStep(t, currentImbalVol, flex);
        calculateAndDivideBudgets(remediedImbalance);
        payReservationFees(flex);
        nominateAncillaryServiceActivation(currentImbalVol, remediedImbalance);
    }

    private void nominateAncillaryServiceActivation(int currentImbalVol,
            int remediedImbalance) {
        Nomination n = Nomination.create(currentImbalVol, remediedImbalance);
        for (AncilServiceNominationManager asnm : nominationManagers) {
            asnm.registerNomination(n);
        }
    }

    private void calculateAndDivideBudgets(int targetFlex) {
        int currentImbalancePrice = imbalancePricing.getCurrentPrice();
        int budget = Math.abs(targetFlex) * currentImbalancePrice;
        int incentives = (int) (budget * (activationPortion + reservePortion));
        dispatchBudgets(incentives);
    }

    private void dispatchBudgets(int incentives) {
        for (RenumerationMediator m : paymentMapper.values()) {
            m.setBudget(incentives);
        }
    }

    private void payReservationFees(Multimap<SiteFlexAPI, FlexTuple> flex) {
        int sumFlex = 0;
        Map<SiteFlexAPI, Integer> portions = Maps.newLinkedHashMap();
        for (SiteFlexAPI api : flex.keySet()) {
            int maxFlexInProfile = CollectionUtils.max(flex.get(api),
                    new IntNNFunction<FlexTuple>() {

                        @Override
                        public int apply(FlexTuple input) {
                            return input.getDeltaP();
                        }
                    });
            sumFlex += maxFlexInProfile;
            portions.put(api, maxFlexInProfile);
        }
        for (Entry<SiteFlexAPI, Integer> entry : portions.entrySet()) {
            getActualPaymentMediatorFor(entry.getKey()).registerReservation(
                    entry.getValue() / (double) sumFlex);
        }
    }

    private void payActivationFees(Multimap<SiteFlexAPI, FlexTuple> flex,
            Set<Long> ids) {
        int sumFlex = 0;
        Map<SiteFlexAPI, Integer> portions = Maps.newLinkedHashMap();
        for (SiteFlexAPI api : flex.keySet()) {
            for (long i : ids) {
                for (FlexTuple t : flex.get(api)) {
                    if (t.getId() == i) {
                        sumFlex += t.getDeltaP();
                        portions.put(api, t.getDeltaP());
                    }
                }
            }
        }
        for (Entry<SiteFlexAPI, Integer> entry : portions.entrySet()) {
            getActualPaymentMediatorFor(entry.getKey()).registerActivation(
                    entry.getValue() / (double) sumFlex);
        }
    }

    @Override
    protected AggregationContext getAggregationContext() {
        return new AggregationDispatch(super.getAggregationContext());
    }

    /**
     * Register a nomination manager to this Aggregator.
     *
     * @param manager
     *            The manager to register.
     */
    public void registerNominationManager(AncilServiceNominationManager manager) {
        this.nominationManagers.add(manager);
    }

    private class AggregationDispatch implements AggregationContext {
        private final AggregationContext delegate;

        /**
         * Default constructor for this delegating dispatch
         *
         * @param delegate
         *            the target to delegate to.
         */
        public AggregationDispatch(AggregationContext delegate) {
            this.delegate = delegate;
        }

        @Override
        public void dispatchActivation(Multimap<SiteFlexAPI, FlexTuple> flex,
                Set<Long> ids) {
            delegate.dispatchActivation(flex, ids);
            payActivationFees(flex, ids);
        }
    }
}
