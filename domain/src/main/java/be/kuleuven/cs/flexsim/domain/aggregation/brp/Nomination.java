package be.kuleuven.cs.flexsim.domain.aggregation.brp;

import com.google.auto.value.AutoValue;

/**
 * A nomination value class for registering nominations for the activation of
 * ancillary services.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@AutoValue
public abstract class Nomination {

    Nomination() {
    }

    /**
     * @return the target imbalance volume that needs to be corrected.
     */
    public abstract long getTargetImbalanceVolume();

    /**
     * @return the target imbalance volume that was effectively remedied.
     */
    public abstract long getRemediedImbalanceVolume();

    /**
     * Factory method
     *
     * @param target the target imbalance volume that needs to be corrected.
     * @param result the target imbalance volume that was effectively remedied.
     * @return a nomination value object.
     */
    static Nomination create(final long target, final long result) {
        return new AutoValue_Nomination(target, result);
    }
}
