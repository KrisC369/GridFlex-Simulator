package be.kuleuven.cs.gridflex.domain.energy.dso.r3dp;

/**
 * Representation of the constraint concerning flexibility activation.
 * This is similar to a contract between provider and SO stating the allowed boundaries of
 * operation when dealing with energy flexibility.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public interface FlexConstraints {
    /**
     * @return The number of time steps allowed between activations.
     */
    double getInterActivationTime();

    /**
     * @return The maximum number of consecutive time steps activation is allowed.
     */
    double getActivationDuration();

    /**
     * @return The maximum amount of allowed activations
     */
    double getMaximumActivations();
}
