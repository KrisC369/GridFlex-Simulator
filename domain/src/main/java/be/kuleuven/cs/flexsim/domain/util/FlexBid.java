package be.kuleuven.cs.flexsim.domain.util;

import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

/**
 * Data class representing a bid for flexibility.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class FlexBid implements AbstractBid {
    private final FlexTuple flex;
    private final int valuation;

    /**
     * Default constructor.
     * 
     * @param flex
     *            The flexibility tuple.
     * @param valuation
     *            the compensation requested
     */
    public FlexBid(final FlexTuple flex, final int valuation) {
        this.flex = flex;
        this.valuation = valuation;

    }

    /**
     * @return the flex
     */
    public final FlexTuple getFlex() {
        return flex;
    }

    /**
     * @return the compensation
     */
    @Override
    public final int getValuation() {
        return valuation;
    }
}
