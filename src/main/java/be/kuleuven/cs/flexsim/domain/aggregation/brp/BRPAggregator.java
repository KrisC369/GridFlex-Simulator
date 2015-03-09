package be.kuleuven.cs.flexsim.domain.aggregation.brp;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;

import be.kuleuven.cs.flexsim.domain.aggregation.independent.IndependentAggregator;
import be.kuleuven.cs.flexsim.domain.energy.tso.contractual.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTracker;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteFlexAPI;

import com.google.common.collect.Maps;

/**
 * Represents an BRP-aggregator trying to balance a portfolio.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class BRPAggregator extends IndependentAggregator {

    private Map<SiteFlexAPI, RenumerationMediator> paymentMapper;
    private double activationPortion;
    private double reservePortion;

    /**
     * @param tso
     * @param frequency
     */
    public BRPAggregator(BalancingSignal tso, double reservation,
            double activation) {
        super(tso, 1);
        this.paymentMapper = Maps.newLinkedHashMap();
        this.activationPortion = activation;
        this.reservePortion = reservation;
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
        return RenumerationMediator.create(client, reservePortion,
                activationPortion);
    }

}
