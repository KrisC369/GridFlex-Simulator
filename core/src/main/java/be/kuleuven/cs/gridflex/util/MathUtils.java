package be.kuleuven.cs.gridflex.util;

/**
 * Class for math-based utility functions.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class MathUtils {
    private static final double EPSILON = 0.00001d;
    private static final double ZERO = 0d;

    private MathUtils() {
    }

    /**
     * Calculates the number of multicombinations of n elements in sets of
     * k-sized bags. Formula: (n+k-1)! / n! * (k-1)!
     *
     * @param k the k-param.
     * @param n the n-param.
     * @return the amount of combinations.
     */
    public static long multiCombinationSize(final int k, final int n) {
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

    /**
     * Calculates the positive surface under the trapezoid formed by the
     * arguments.
     *
     * @param n1y The y coordinate of the first number. (The x coord is 0.)
     * @param n2y the y coordinate of the second number.
     * @param n2x The x coordinate of the second number.
     * @return the positive surface area.
     */
    public static double trapzPos(final double n1y, final double n2y, final double n2x) {
        if (n1y * n2y > 0) {
            return trapzPosGtZero(n1y, n2y, n2x);
        } else if (n1y * n2y < 0) {
            return trapzPosLtZero(n1y, n2y, n2x);
        } else {
            return trapzPosEqZero(n1y, n2y, n2x);
        }
    }

    private static double trapzPosEqZero(final double n1y, final double n2y, final double n2x) {
        if (n2y > n1y) {
            return trapzPosEqZeroFirstLarger(n1y, n2y, n2x);
        } else if (n2y < n1y) {
            return trapzPosEqZeroFirstSmaller(n1y, n2x);
        }
        return 0;
    }

    private static double trapzPosEqZeroFirstSmaller(final double n1y, final double n2x) {
        if (n1y > 0) {
            return case6(n1y, n2x);
        } else if (n1y < EPSILON) {
            return ZERO;
        }
        return 0;
    }

    private static double trapzPosEqZeroFirstLarger(final double n1y, final double n2y,
            final double n2x) {
        if (n1y < 0) {
            return ZERO;
        } else if (n1y < EPSILON) {
            return case8(n2y, n2x);
        }
        return 0;
    }

    private static double trapzPosLtZero(final double n1y, final double n2y, final double n2x) {
        if (n2y > n1y) {
            return case4(n1y, n2y, n2x);
        }
        return case3(n1y, n2y, n2x);
    }

    private static double trapzPosGtZero(final double n1y, final double n2y, final double n2x) {
        if (n1y > 0) {
            if (n2y > n1y) {
                return case1(n1y, n2y, n2x);
            }
            return case2(n1y, n2y, n2x);
        } else if (n1y < 0) {
            return ZERO;
        }
        return 0;
    }

    private static double case1(final double n1y, final double n2y, final double n2x) {
        return n2x * (n1y + (n2y - n1y) / 2d);
    }

    private static double case2(final double n1y, final double n2y, final double n2x) {
        return n2x * (n2y + (n1y - n2y) / 2d);
    }

    private static double case4(final double n1y, final double n2y, final double n2x) {
        final double a = getA(n1y, n2y, n2x);
        return ((n2x - a) * n2y) / 2d;
    }

    private static double case3(final double n1y, final double n2y, final double n2x) {
        final double a = getA(n1y, n2y, n2x);
        return (a * n1y) / 2d;
    }

    private static double getA(final double n1y, final double n2y, final double n2x) {
        return (-n1y * n2x) / (n2y - n1y);
    }

    private static double case8(final double n2y, final double n2x) {
        return (n2y * n2x) / 2d;
    }

    private static double case6(final double n1y, final double n2x) {
        return (n1y * n2x) / 2d;
    }

}
