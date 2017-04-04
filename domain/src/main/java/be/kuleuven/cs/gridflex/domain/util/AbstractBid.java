package be.kuleuven.cs.gridflex.domain.util;

/**
 * Represents an abstract bid in the context of an abstract auction.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */@FunctionalInterface
public interface AbstractBid {
    /**
     * @return the compensation wanted for this bid.
     */
    int getValuation();
}
