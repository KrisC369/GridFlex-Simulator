package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.gametheory.results.HeuristicSymmetricPayoffMatrix;
import be.kuleuven.cs.gametheory.stats.ConfidenceLevel;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class HeuristicSymmetricPayoffMatrixTest {
    private static final double DELTA = 0.001;
    private HeuristicSymmetricPayoffMatrix table = mock(
            HeuristicSymmetricPayoffMatrix.class);
    private int agents = 3;
    private int actions = 2;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.table = new HeuristicSymmetricPayoffMatrix(agents, actions);
    }

    @Test
    public void testInit() {
        assertFalse(this.table.isComplete());
    }

    @Test
    public void testCompleteFalse() {
        Double[] value = new Double[] { 34d, 34d, 34d };
        table.addEntry(value, 1, 2);
        assertFalse(this.table.isComplete());

        this.table = new HeuristicSymmetricPayoffMatrix(agents, 3);
        table.addEntry(value, 1, 0, 2);
        assertFalse(this.table.isComplete());
    }

    @Test
    public void testAddEntryInvalid() {
        Double[] value = new Double[] { 34d, 34d, 34d };
        exception.expect(IllegalArgumentException.class);
        table.addEntry(value, 1, 3);
    }

    @Test
    public void testAddEntryInvalid2() {
        Double[] value = new Double[] { 34d, 34d, 34d };
        exception.expect(IllegalArgumentException.class);
        table.addEntry(value, 1, 3, 4, 3);
    }

    @Test
    public void testAddEntryInvalid3() {
        Double[] value = new Double[] { 34d, 34d, 34d, 14d };
        exception.expect(IllegalArgumentException.class);
        table.addEntry(value, 1, 2);
    }

    @Test
    public void testAddEntryInvalidGet() {
        Double[] value = new Double[] { 34d, 34d, 34d };
        table.addEntry(value, 1, 2);
        exception.expect(IllegalArgumentException.class);
        table.getEntry(2, 1);
    }

    @Test
    public void testCompleteTrue() {
        Double[] value = new Double[] { 34d, 34d, 34d };
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value, agents - i, i);
        }
        assertTrue(table.isComplete());
    }

    @Test
    public void testDoubleGetTrue() {
        Double[] value = new Double[] { 34d, 34d, 34d };
        Double[] value2 = new Double[] { 17d, 17d, 17d };
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value, agents - i, i);
        }
        assertTrue(table.isComplete());
        table.addEntry(value2, 3, 0);
        assertTrue(table.getEntry(3, 0)[0] < value[0]);
    }

    @Test
    public void testAvg() {
        this.agents = 1;
        this.table = new HeuristicSymmetricPayoffMatrix(agents, actions);
        int high = 34;
        int low = 17;
        int higher = 53;
        Double[] value = new Double[] { (double) high };
        Double[] value2 = new Double[] { (double) low };
        Double[] value3 = new Double[] { (double) higher };
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value, agents - i, i);
        }
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value2, agents - i, i);
        }
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value3, agents - i, i);
        }
        assertTrue(table.isComplete());
        for (int i = 0; i <= agents; i++) {
            // table.addEntry(value2, agents - i, i);
            Double[] current = table.getEntry(agents - i, i);
            assertTrue(current[0] > low && current[0] < higher);
            assertEquals(current[0], (high + low + higher) / 3.0, 0.5);
        }
    }

    @Test
    public void test2by2() {
        this.table = new HeuristicSymmetricPayoffMatrix(2, 2);
        table.addEntry(new Double[] { 5d, 15d }, 2, 0);
        table.addEntry(new Double[] { 20d, 25d }, 1, 1);
        table.addEntry(new Double[] { 20d, 40d }, 0, 2);

        Double[] result = table.getEntry(1, 1);
        Double[] resultVar = table.getVariance(1, 1);

        // Test values:
        assertEquals(20, result[0], 0);
        assertEquals(25, result[1], 0);
        assertEquals(0, resultVar[0], 0);
        assertEquals(0, resultVar[1], 0);

        table.addEntry(new Double[] { 20d, 40d }, 1, 1);
        resultVar = table.getVariance(1, 1);
        assertEquals(0, resultVar[0], 0);
        assertEquals(112.5, resultVar[1], 0);

    }

    @Test
    public void testToString() {
        double valueL = 34d;
        System.out.println(table.toString());
        assertTrue(this.table.toString().isEmpty());
        Double[] value = new Double[] { valueL, valueL, valueL };
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value, agents - i, i);
        }
        assertFalse(this.table.toString().isEmpty());
        assertTrue(this.table.toString().contains(String.valueOf(valueL)));
    }

    @Test
    public void testExternalityCI() {
        table.addExternalityValue(10);
        table.addExternalityValue(10);
        table.addExternalityValue(10);
        table.addExternalityValue(20);
        table.addExternalityValue(20);
        ConfidenceInterval ci = table.getExternalityCI(ConfidenceLevel._95pc);
        System.out.println(ci);
        assertTrue(ci.getLowerBound() > 9);
        assertTrue(ci.getUpperBound() < 20);
        assertEquals(14, ci.getLowerBound() + ((ci.getUpperBound() - ci.getLowerBound()) / 2d),
                0.001);
    }

    @Test
    public void testExternalityCINAN() {
        table.addExternalityValue(10);
        table.addExternalityValue(10);
        table.addExternalityValue(10);
        table.addExternalityValue(20);
        table.addExternalityValue(20);
        table.addExternalityValue(0.0 / 0.0);
        ConfidenceInterval ci = table.getExternalityCI(ConfidenceLevel._95pc);
        System.out.println(ci);
        assertTrue(ci.getLowerBound() > 9);
        assertTrue(ci.getUpperBound() < 20);
        assertEquals(14, ci.getLowerBound() + ((ci.getUpperBound() - ci.getLowerBound()) / 2d),
                0.001);
    }

    @Test
    public void testExternalityCIMultiSampleSize() {
        table.addExternalityValue(10);
        table.addExternalityValue(10);
        table.addExternalityValue(10);
        table.addExternalityValue(20);
        table.addExternalityValue(20);
        ConfidenceInterval ci = table.getExternalityCI(ConfidenceLevel._95pc);
        table.addExternalityValue(10);
        table.addExternalityValue(10);
        table.addExternalityValue(10);
        table.addExternalityValue(20);
        table.addExternalityValue(20);
        ConfidenceInterval ci2 = table.getExternalityCI(ConfidenceLevel._95pc);
        assertTrue(ci.getLowerBound() - ci2.getLowerBound() < DELTA);
        assertTrue(ci.getUpperBound() - ci2.getUpperBound() > DELTA);
    }
}
