package be.kuleuven.cs.gametheory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HeuristicSymmetricPayoffMatrixTest {
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
        long[] value = new long[] { 34, 34, 34 };
        table.addEntry(value, 1, 2);
        assertFalse(this.table.isComplete());

        this.table = new HeuristicSymmetricPayoffMatrix(agents, 3);
        table.addEntry(value, 1, 0, 2);
        assertFalse(this.table.isComplete());
    }

    @Test
    public void testAddEntryInvalid() {
        long[] value = new long[] { 34, 34, 34 };
        exception.expect(IllegalArgumentException.class);
        table.addEntry(value, 1, 3);
    }

    @Test
    public void testAddEntryInvalid2() {
        long[] value = new long[] { 34, 34, 34 };
        exception.expect(IllegalArgumentException.class);
        table.addEntry(value, 1, 3, 4, 3);
    }

    @Test
    public void testAddEntryInvalid3() {
        long[] value = new long[] { 34, 34, 34, 14 };
        exception.expect(IllegalArgumentException.class);
        table.addEntry(value, 1, 2);
    }

    @Test
    public void testAddEntryInvalidGet() {
        long[] value = new long[] { 34, 34, 34 };
        table.addEntry(value, 1, 2);
        exception.expect(IllegalArgumentException.class);
        table.getEntry(2, 1);
    }

    @Test
    public void testCompleteTrue() {
        long[] value = new long[] { 34, 34, 34 };
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value, agents - i, i);
        }
        assertTrue(table.isComplete());
    }

    @Test
    public void testDoubleGetTrue() {
        long[] value = new long[] { 34, 34, 34 };
        long[] value2 = new long[] { 17, 17, 17 };
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
        long[] value = new long[] { high };
        long[] value2 = new long[] { low };
        long[] value3 = new long[] { higher };
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
            double[] current = table.getEntry(agents - i, i);
            assertTrue(current[0] > low && current[0] < higher);
            assertEquals(current[0], (high + low + higher) / 3.0, 0.5);
        }
    }

    @Test
    public void testGetDynamicsArgs() {
        int reward = 39;
        long[] value = new long[] { reward, reward, reward };
        for (int i = 0; i <= agents; i++) {
            reward -= 5;
            value = new long[] { reward, reward, reward };
            table.addEntry(value, agents - i, i);
        }
        List<Double> result = table.getDynamicEquationFactors();

        // Test values:
        assertEquals(34, result.get(0), 0);
        assertEquals(29, result.get(1), 0);
        assertEquals(29, result.get(2), 0);
        assertEquals(24, result.get(3), 0);
        assertEquals(24, result.get(4), 0);
        assertEquals(19, result.get(5), 0);
    }

    @Test
    public void testGetDynamicsArgs3S() {
        agents = 2;
        actions = 3;
        this.table = new HeuristicSymmetricPayoffMatrix(agents, actions);
        int reward = 39;
        long[] value = new long[] { reward, reward };
        for (int i = 0; i <= agents; i++) {
            reward -= 5;
            value = new long[] { reward, reward };
            table.addEntry(value, agents - i, i, 0);
        }
        reward -= 5;
        value = new long[] { reward, reward };
        table.addEntry(value, 1, 0, 1);
        reward -= 5;
        value = new long[] { reward, reward };
        table.addEntry(value, 0, 1, 1);
        reward -= 5;
        value = new long[] { reward, reward };
        table.addEntry(value, 0, 0, 2);

        List<Double> result = table.getDynamicEquationFactors();
        assertEquals(9, result.size(), 0);
        // Test values:
        assertEquals(34, result.get(0), 0);
        assertEquals(29, result.get(1), 0);
        assertEquals(29, result.get(2), 0);
        assertEquals(24, result.get(3), 0);
        assertEquals(19, result.get(5), 0);
        assertEquals(14, result.get(6), 0);
        assertEquals(14, result.get(7), 0);
        assertEquals(9, result.get(8), 0);
    }

    @Test
    public void testToString() {
        int valueL = 34;
        System.out.println(table.toString());
        assertTrue(this.table.toString().isEmpty());
        long[] value = new long[] { valueL, valueL, valueL };
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value, agents - i, i);
        }
        assertFalse(this.table.toString().isEmpty());
        assertTrue(this.table.toString().contains(String.valueOf(valueL)));
    }
}
