package be.kuleuven.cs.gridflex.domain.aggregation.r3dp;

import be.kuleuven.cs.gridflex.domain.util.data.profiles.DayAheadPriceProfile;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.PositiveImbalancePriceProfile;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class BudgetTrackerTest {
    private static final String FILENAME_IMBALANCE = "imbalancePricesTest.csv";
    private static final String FILENAME_DAYAHEAD = "dayAheadPricesTest.csv";
    private static final double FIXED_SELLING_PRICE = 35d;
    public static final double DELTA = 0.001;
    private static String pipColumn = "PPOS";
    private static String dapColumn = "dap";

    private BudgetTracker targetFixed;
    private BudgetTracker targetVariable;

    @Before
    public void setUp() throws Exception {
        PositiveImbalancePriceProfile ppos = PositiveImbalancePriceProfile
                .createFromCSV(FILENAME_IMBALANCE, pipColumn);
        DayAheadPriceProfile pda = DayAheadPriceProfile
                .createFromCSV(FILENAME_DAYAHEAD, dapColumn);
        this.targetFixed = BudgetTracker.createFixedSellingPrice(ppos, FIXED_SELLING_PRICE);
        this.targetVariable = BudgetTracker.createDayAheadSellingPrice(ppos, pda);
    }

    @Test
    public void testFixedGetBudgetForPeriod() {
        double expectedPPOS = 11;
        for (int i = 0; i < targetFixed.getTotalBudgetPeriods(); i++) {
            assertEquals(FIXED_SELLING_PRICE - expectedPPOS, targetFixed.getBudgetForPeriod(i), 0);
        }
    }

    @Test
    public void testVariableGetBudgetForPeriod() {
        double expectedPPOS = 11;
        double expectedPAD = 45;
        for (int i = 0; i < targetVariable.getTotalBudgetPeriods(); i++) {
            assertEquals(expectedPAD - expectedPPOS, targetVariable.getBudgetForPeriod(i),
                    0);
        }
    }

    @Test
    public void testRealSizeProfileWholeYear() throws Exception {
        PositiveImbalancePriceProfile ppos = PositiveImbalancePriceProfile
                .createFromCSV("imbalance_prices.csv", pipColumn);
        DayAheadPriceProfile pda = DayAheadPriceProfile
                .extrapolateFromHourlyOneDayData("dailyDayAheadPrices.csv", "damhp", 365);
        BudgetTracker target = BudgetTracker.createDayAheadSellingPrice(ppos, pda);
        int idx = 2 + 2 * 4 + 2 * 24 * 4;
        double exp = 44.85 - (-29.63);
        assertEquals(exp, target.getBudgetForPeriod(idx), DELTA);
    }

    @Test
    //@Ignore
    public void testRealSizeProfileWholeYearNoNeg() throws Exception {
        PositiveImbalancePriceProfile ppos = PositiveImbalancePriceProfile
                .createFromCSV("imbalance_prices.csv", pipColumn);
        DayAheadPriceProfile pda = DayAheadPriceProfile
                .extrapolateFromHourlyOneDayData("dailyDayAheadPrices.csv", "damhp", 365);
        BudgetTracker target = BudgetTracker.createDayAheadSellingPrice(ppos, pda);
        int neg = 0;
        int pos = 0;
        for (int i = 0; i < target.getTotalBudgetPeriods(); i++) {
            if (target.getBudgetForPeriod(i) < 0) {
                neg++;
            } else if (target.getBudgetForPeriod(i) > 0) {
                pos++;
            }
        }
        double posPerc = pos / (double) target.getTotalBudgetPeriods();
        double negPerc = neg / (double) target.getTotalBudgetPeriods();
        System.out.println("pos: " + posPerc);
        System.out.println("neg: " + negPerc);
        System.out.println("zero: " + (1-(negPerc+posPerc)));
    }
}