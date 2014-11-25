package be.kuleuven.cs.gametheory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HeuristicSymmetricPayoffMatrixTest {
    private HeuristicSymmetricPayoffMatrix table = mock(HeuristicSymmetricPayoffMatrix.class);
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
        int[] value = new int[] { 34, 34, 34 };
        table.addEntry(value, 1, 2);
        assertFalse(this.table.isComplete());

        this.table = new HeuristicSymmetricPayoffMatrix(agents, 3);
        table.addEntry(value, 1, 0, 2);
        assertFalse(this.table.isComplete());
    }

    @Test
    public void testAddEntryInvalid() {
        int[] value = new int[] { 34, 34, 34 };
        exception.expect(IllegalArgumentException.class);
        table.addEntry(value, 1, 3);
    }

    @Test
    public void testAddEntryInvalid2() {
        int[] value = new int[] { 34, 34, 34 };
        exception.expect(IllegalArgumentException.class);
        table.addEntry(value, 1, 3, 4, 3);
    }

    @Test
    public void testAddEntryInvalidGet() {
        int[] value = new int[] { 34, 34, 34 };
        table.addEntry(value, 1, 2);
        exception.expect(IllegalArgumentException.class);
        table.getEntry(2, 1);
    }

    @Test
    public void testCompleteTrue() {
        int[] value = new int[] { 34, 34, 34 };
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value, agents - i, i);
        }
        assertTrue(table.isComplete());
    }

    @Test
    public void testDoubleGetTrue() {
        int[] value = new int[] { 34, 34, 34 };
        int[] value2 = new int[] { 17, 17, 17 };
        for (int i = 0; i <= agents; i++) {
            table.addEntry(value, agents - i, i);
        }
        assertTrue(table.isComplete());
        table.addEntry(value2, 3, 0);
        assertTrue(table.getEntry(3, 0)[0] < value[0]);
    }
}
