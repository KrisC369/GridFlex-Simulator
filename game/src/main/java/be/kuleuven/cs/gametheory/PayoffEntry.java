package be.kuleuven.cs.gametheory;

import com.google.auto.value.AutoValue;

/**
 * Class representing an entry in the payoff table. Keeps tracks of indices for
 * values.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@AutoValue
abstract class PayoffEntry {

    abstract int[] getEntries();

    static PayoffEntry from(final int... key) {
        return new AutoValue_PayoffEntry(key);
    }
}
