package be.kuleuven.cs.flexsim.domain.energy.tso.auctioning;

import be.kuleuven.cs.flexsim.domain.energy.tso.MechanismHost;
import be.kuleuven.cs.flexsim.domain.energy.tso.MechanismParticipant;
import be.kuleuven.cs.flexsim.domain.util.AbstractBid;

import java.util.Collection;

/**
 * Interface representing an abstract auction where one can place bids.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The type of bids to accept in this auction.
 */
public interface AbstractAuctionHost<T extends AbstractBid>
        extends MechanismHost<MechanismParticipant> {

    /**
     * Return the outstanding bids of this auction.
     * 
     * @return A collection of outstanding bids.
     */
    Collection<? extends AbstractBid> getCurrentOutstandingBids();

    /**
     * Place a bid in this auction.
     * 
     * @param bid
     *            The bid to place.
     */
    void placeBid(T bid);

}
