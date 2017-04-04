package be.kuleuven.cs.gridflex.domain.aggregation.brp;

import be.kuleuven.cs.gridflex.domain.aggregation.AggregationContext;
import be.kuleuven.cs.gridflex.domain.aggregation.independent.IndependentAggregator;
import be.kuleuven.cs.gridflex.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.gridflex.domain.finance.FinanceTracker;
import be.kuleuven.cs.gridflex.domain.site.Site;
import be.kuleuven.cs.gridflex.domain.site.SiteFlexAPI;
import be.kuleuven.cs.gridflex.domain.util.CollectionUtils;
import be.kuleuven.cs.gridflex.domain.util.FlexTuple;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents an BRP-aggregator trying to balance a portfolio.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class BRPAggregator extends IndependentAggregator {

    private final Map<SiteFlexAPI, RemunerationMediator> paymentMapper;
    private final double activationPortion;
    private final double reservePortion;
    private final PriceSignal imbalancePricing;
    private final List<AncilServiceNominationManager> nominationManagers;

    /**
     * Default constructor.
     *
     * @param tso         The balancing signal to follow.
     * @param pricing     The pricing signal for the imbalance prices.
     * @param reservation The reservation payment portion marker.
     * @param activation  The activation payment portion marker.
     */
    public BRPAggregator(final BalancingSignal tso, final PriceSignal pricing, final double reservation,
            final double activation) {
        super(tso, 1);
        checkArgument(
                reservation + activation >= 0 && reservation + activation <= 1,
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
     * @param client The site to register.
     */
    public void registerClient(final Site client) {
        super.registerClient(client);
        this.paymentMapper.put(client, createMediator(client));
    }

    private RemunerationMediator createMediator(final Site client) {
        return RemunerationMediator.create(client, reservePortion);
    }

    /**
     * Get the financetracker for the SiteFlexAPI in question.
     *
     * @param s the API reference.
     * @return The registered FinanceTracker.
     */
    public FinanceTracker getFinanceTrackerFor(final SiteFlexAPI s) {
        return getActualPaymentMediatorFor(s);
    }

    RemunerationMediator getActualPaymentMediatorFor(final SiteFlexAPI s) {
        checkArgument(paymentMapper.containsKey(s),
                "Invalid argument: Site not registered as client.");
        return this.paymentMapper.get(s);
    }

    @Override
    public void tick(final int t) {
        // Get target and budget and set budgets.
        final int currentImbalVol = getTargetFlex();
        // Make reservation payments
        final Multimap<SiteFlexAPI, FlexTuple> flex = gatherFlexInfo();
        final int remediedImbalance = doAggregationStep(t, currentImbalVol, flex);
        calculateAndDivideBudgets(remediedImbalance);
        payReservationFees(flex);
        nominateAncillaryServiceActivation(currentImbalVol, remediedImbalance);
    }

    private void nominateAncillaryServiceActivation(final int currentImbalVol,
            final int remediedImbalance) {
        final Nomination n = Nomination.create(currentImbalVol, remediedImbalance);
        for (final AncilServiceNominationManager asnm : nominationManagers) {
            asnm.registerNomination(n);
        }
    }

    private void calculateAndDivideBudgets(final int targetFlex) {
        final int currentImbalancePrice = imbalancePricing.getCurrentPrice();
        final int budget = Math.abs(targetFlex) * currentImbalancePrice;
        final int incentives = (int) (budget * (activationPortion + reservePortion));
        dispatchBudgets(incentives);
    }

    private void dispatchBudgets(final int incentives) {
        for (final RemunerationMediator m : paymentMapper.values()) {
            m.setBudget(incentives);
        }
    }

    private void payReservationFees(final Multimap<SiteFlexAPI, FlexTuple> flex) {
        int sumFlex = 0;
        final Map<SiteFlexAPI, Integer> portions = Maps.newLinkedHashMap();
        for (final SiteFlexAPI api : flex.keySet()) {
            final int maxFlexInProfile = CollectionUtils.max(flex.get(api),
                    FlexTuple::getDeltaP);
            sumFlex += maxFlexInProfile;
            portions.put(api, maxFlexInProfile);
        }
        for (final Entry<SiteFlexAPI, Integer> entry : portions.entrySet()) {
            getActualPaymentMediatorFor(entry.getKey())
                    .registerReservation(entry.getValue() / (double) sumFlex);
        }
    }

    private void payActivationFees(final Multimap<SiteFlexAPI, FlexTuple> flex,
            final Set<Long> ids) {
        int sumFlex = 0;
        final Map<SiteFlexAPI, Integer> portions = Maps.newLinkedHashMap();
        for (final SiteFlexAPI api : flex.keySet()) {
            for (final long i : ids) {
                for (final FlexTuple t : flex.get(api)) {
                    if (t.getId() == i) {
                        sumFlex += t.getDeltaP();
                        portions.put(api, t.getDeltaP());
                    }
                }
            }
        }
        for (final Entry<SiteFlexAPI, Integer> entry : portions.entrySet()) {
            getActualPaymentMediatorFor(entry.getKey())
                    .registerActivation(entry.getValue() / (double) sumFlex);
        }
    }

    @Override
    protected AggregationContext getAggregationContext() {
        return new AggregationDispatch(super.getAggregationContext());
    }

    /**
     * Register a nomination manager to this Aggregator.
     *
     * @param manager The manager to register.
     */
    public void registerNominationManager(
            final AncilServiceNominationManager manager) {
        this.nominationManagers.add(manager);
    }

    private class AggregationDispatch implements AggregationContext {
        private final AggregationContext delegate;

        /**
         * Default constructor for this delegating dispatch
         *
         * @param delegate the target to delegate to.
         */
        public AggregationDispatch(final AggregationContext delegate) {
            this.delegate = delegate;
        }

        @Override
        public void dispatchActivation(final Multimap<SiteFlexAPI, FlexTuple> flex,
                final Set<Long> ids) {
            delegate.dispatchActivation(flex, ids);
            payActivationFees(flex, ids);
        }
    }
}
