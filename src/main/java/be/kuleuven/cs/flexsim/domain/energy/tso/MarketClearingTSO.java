package be.kuleuven.cs.flexsim.domain.energy.tso;

import java.util.Collection;
import java.util.List;

import be.kuleuven.cs.flexsim.domain.energy.consumption.EnergyConsumptionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.util.FlexBid;

import com.google.common.collect.Lists;

/**
 * A TSO implementation that can accept bids for balancing actions and clears
 * the bids, optimally selecting the best choices.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class MarketClearingTSO extends CopperplateTSO implements
        AbstractAuction<FlexBid> {
    private List<AuctionParticipant> participants;
    private List<FlexBid> currentBids;

    /**
     * Constructor with consumption instances as parameter.
     * 
     * @param sites
     *            The consumption sites connected to this TSO
     */
    public MarketClearingTSO(EnergyConsumptionTrackable... sites) {
        this(new EnergyProductionTrackable[0], sites);
    }

    /**
     * Constructor with production instances as parameter.
     * 
     * @param sites
     *            The production sites connected to this TSO
     */
    public MarketClearingTSO(EnergyProductionTrackable... sites) {
        this(sites, new EnergyConsumptionTrackable[0]);
    }

    /**
     * Constructor with no initial partakers.
     * 
     */
    public MarketClearingTSO() {
        this(new EnergyProductionTrackable[0],
                new EnergyConsumptionTrackable[0]);
    }

    /**
     * Actual initializing constructor.
     * 
     * @param prod
     *            the producers.
     * @param cons
     *            the consumers.
     */
    private MarketClearingTSO(EnergyProductionTrackable[] prod,
            EnergyConsumptionTrackable[] cons) {
        super(prod, cons);
        this.participants = Lists.newArrayList();
        this.currentBids = Lists.newArrayList();
    }

    @Override
    public void placeBid(FlexBid bid) {
        this.currentBids.add(bid);
    }

    @Override
    public Collection<FlexBid> getCurrentOutstandingBids() {
        return Lists.newArrayList(currentBids);
    }

    @Override
    public void registerParticipant(AuctionParticipant participant) {
        this.participants.add(participant);
    }

    /**
     * @return the participants
     */
    public List<AuctionParticipant> getParticipants() {
        return participants;
    }

    @Override
    public void tick(int t) {
        super.tick(t);
    }

    @Override
    public void afterTick(int t) {
        super.afterTick(t);
        clearMarket();
        resetBids();
    }

    private void clearMarket() {

    }

    private void resetBids() {
        this.currentBids = Lists.newArrayList();
    }

}
