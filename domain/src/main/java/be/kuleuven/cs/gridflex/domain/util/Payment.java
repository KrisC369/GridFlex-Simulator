package be.kuleuven.cs.gridflex.domain.util;

import com.google.auto.value.AutoValue;

/**
 * Represents a Payment in money.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class Payment {
    Payment() {
    }

    /**
     * @return The amount of money transferred via this payment.
     */
    public abstract double getMonetaryAmount();

    /**
     * Default factory method.
     *
     * @param amount The amount of money transferred via this payment.
     * @return A payment instance.
     */
    public static Payment create(double amount) {
        return new AutoValue_Payment(amount);
    }
}
