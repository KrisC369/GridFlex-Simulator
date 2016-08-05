package be.kuleuven.cs.flexsim.domain.aggregation.brp;

/**
 * Represents a price signal that varies in time.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */@FunctionalInterface
public interface PriceSignal {

    /**
     * Get the current value of the pricing signal.
     *
     * @return the value.
     */
    int getCurrentPrice();
}
