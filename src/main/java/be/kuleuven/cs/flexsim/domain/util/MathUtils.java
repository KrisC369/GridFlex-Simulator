package be.kuleuven.cs.flexsim.domain.util;

/**
 * Class for math-based utility functions.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class MathUtils {

    private MathUtils() {
    }

    /**
     * Calculates the number of multicombinations of n elements in sets of
     * k-sized bags. Formula: (n+k-1)! / n! * (k-1)!
     * 
     * @param k
     *            the k-param.
     * @param n
     *            the n-param.
     * @return the amount of combinations.
     */
    public static long multiCombinationSize(int k, int n) {
        long result = 1;
        if (n > (k - 1)) {
            for (int i = n + k - 1; i > n; i--) {
                result *= i;
            }
            int factDenom = 1;
            for (int i = 1; i <= k - 1; i++) {
                factDenom *= i;
            }
            result /= factDenom;
        } else {
            for (int i = n + k - 1; i > (k - 1); i--) {
                result *= i;
            }
            int factDenom = 1;
            for (int i = 1; i <= n; i++) {
                factDenom *= i;
            }
            result /= factDenom;
        }
        return result;
    }

}
