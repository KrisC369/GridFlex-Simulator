package be.kuleuven.cs.flexsim.domain.energy.dso.offline.r3dp;

/**
 * A provider of flexibility with activation constraints.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class FlexProvider {
    private final HourlyFlexConstraints constraints;
    private final double powerRate;

    /**
     * Constructor
     *
     * @param powerRate The powerrate this provider can offer.
     */
    public FlexProvider(final double powerRate) {
        this(powerRate, HourlyFlexConstraints.R3DP);
    }

    /**
     * Constructor with contract.
     *
     * @param powerRate The powerrate this provider can offer.
     * @param contract  The contract containing the flexibility constraints agreed upon.
     */
    public FlexProvider(final double powerRate, final HourlyFlexConstraints contract) {
        this.constraints = contract;
        this.powerRate = powerRate;
    }

    /**
     * @return The activation constraints.
     */
    public HourlyFlexConstraints getActivationConstraints() {
        return constraints;
    }

    /**
     * @return The flexible power rate.
     */
    public double getPowerRate() {
        return powerRate;
    }
}
