package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.PositiveImbalancePriceProfile;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This class represents the calculation of budgets for each settlement time period, based on
 * different pricing profiles.
 * This budget can be used for compensation of consumption increase because it represents the
 * losses from having to sell surpluss energy in profile to the TSO while having bought it in the
 * Day ahead market (as an example).
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class BudgetTracker {

    private final PositiveImbalancePriceProfile ppos;
    private final DayAheadPriceProfile pda;

    /**
     * Constructor.
     *
     * @param ppos The positive imbalance price profile.
     * @param pda  The price at which energy is sold to consumers.
     * @throws IllegalArgumentException If both profiles don't have equal lengths.
     */
    private BudgetTracker(PositiveImbalancePriceProfile ppos, DayAheadPriceProfile pda) {
        checkArgument(ppos.length() == pda.length(),
                "Both price profiles should be equal in length.");
        this.ppos = ppos;
        this.pda = pda;
    }

    /**
     * Get the price point
     *
     * @param i The index representing the settlement time period for which to return the budget
     *          size.
     * @return The amount of available budget.
     */
    public double getBudgetForPeriod(int i) {
        return pda.value(i) - ppos.value(i);
    }

    /**
     * @return The length of the budget prices profile list.
     */
    int getTotalBudgetPeriods() {
        return ppos.length();
    }

    /**
     * Create a BudgetTracker instance from a day ahead price profile and positive imbalance
     * profile.
     *
     * @param ppos Positive imbalance profile.
     * @param pda  The price for energy on the day ahead market.
     * @return A budget Tracker instance.
     */
    public static BudgetTracker createDayAheadSellingPrice(PositiveImbalancePriceProfile ppos,
            DayAheadPriceProfile pda) {
        return new BudgetTracker(ppos, pda);
    }

    /**
     * Create a BudgetTracker instance from a profile with fixed selling prices and positive
     * imbalance profile.
     *
     * @param ppos              Positive imbalance profile.
     * @param fixedSellingPrice The fixed selling price.
     * @return A budget Tracker instance.
     */
    public static BudgetTracker createFixedSellingPrice(PositiveImbalancePriceProfile ppos,
            final double fixedSellingPrice) {
        DoubleList values = new DoubleArrayList(
                Stream.generate(() -> Double.valueOf(fixedSellingPrice)).limit(ppos.length())
                        .collect(Collectors.toList()));
        return new BudgetTracker(ppos, DayAheadPriceProfile.createFromTimeSeries(() -> values));
    }
}
