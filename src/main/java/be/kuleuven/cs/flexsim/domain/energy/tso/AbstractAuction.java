package be.kuleuven.cs.flexsim.domain.energy.tso;

import java.util.Collection;

import be.kuleuven.cs.flexsim.domain.util.AbstractBid;

/**
 * Interface representing an abstract auction where one can place bids.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * @param <T>
 *            The type of bids to accept in this auction.
 *
 */
public interface AbstractAuction<T extends AbstractBid> {

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

    /**
     * Register an auction participant to this auction.
     * 
     * @param participant
     *            The participant to register.
     */
    void registerParticipant(AuctionParticipant participant);

}
