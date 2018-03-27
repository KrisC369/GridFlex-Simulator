package be.kuleuven.cs.gametheory.util;

import com.google.auto.value.AutoValue;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class Tuple<F, S> {
    public abstract F getFirst();

    public abstract S getSecond();

    public static <F, S> Tuple create(F first, S second) {
        return new AutoValue_Tuple(first, second);
    }
}
