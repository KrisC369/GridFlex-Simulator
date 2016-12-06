package be.kuleuven.cs.flexsim.domain.util;

/**
 * Utility class for array calculations.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * Sums the elements of the arrays.
     *
     * @param first  First array: a
     * @param second Second array: b
     * @return new array with elements e: e_i = a_i + b_i
     */
    public static Double[] arrayAdd(final Double[] first, final Double[] second) {
        final Double[] toret = new Double[first.length];
        for (int i = 0; i < first.length; i++) {
            toret[i] = first[i] + second[i];
        }
        return toret;
    }

    /**
     * Subtracts the elements of the arrays.
     *
     * @param first  First array: a
     * @param second Second array: b
     * @return new array with elements e: e_i = a_i - b_i
     */
    public static Double[] arraySubtract(final Double[] first, final Double[] second) {
        final Double[] toret = new Double[first.length];
        for (int i = 0; i < first.length; i++) {
            toret[i] = first[i] - second[i];
        }
        return toret;
    }

    /**
     * multiplies the elements of the arrays.
     *
     * @param first  First array: a
     * @param second Second array: b
     * @return new array with elements e: e_i = a_i * b_i
     */
    public static Double[] arrayDot(final Double[] first, final Double[] second) {
        final Double[] toret = new Double[first.length];
        for (int i = 0; i < first.length; i++) {
            toret[i] = first[i] * second[i];
        }
        return toret;
    }

    /**
     * Scales the elements of the arrays with a contstant.
     *
     * @param first First array: a
     * @param scale constant: c
     * @return new array with elements e: e_i = a_i *c
     */
    public static Double[] arrayScale(final Double[] first, final double scale) {
        final Double[] toret = new Double[first.length];
        for (int i = 0; i < first.length; i++) {
            toret[i] = first[i] * scale;
        }
        return toret;
    }
}
