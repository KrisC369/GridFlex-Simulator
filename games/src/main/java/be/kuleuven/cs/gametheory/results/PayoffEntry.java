package be.kuleuven.cs.gametheory.results;

import com.google.auto.value.AutoValue;

/**
 * Class representing an entry in the payoff table. Keeps tracks of indices for
 * values.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@AutoValue
public abstract class PayoffEntry {

    PayoffEntry() {
    }

    public abstract int[] getEntries();

    static PayoffEntry from(final int... key) {
        return new AutoValue_PayoffEntry(key);
    }
}
