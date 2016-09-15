package be.kuleuven.cs.flexsim.domain.util.data;

import static be.kuleuven.cs.flexsim.domain.util.FlexTuple.Direction.UP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

import java.util.Formatter;

import be.kuleuven.cs.flexsim.domain.util.FlexTuple;
import org.junit.Before;
import org.junit.Test;

public class FlexTupleTest {

    private FlexTuple tuple = FlexTuple.createNONE();
    private int id = 1;
    private int deltaP = 2;
    private FlexTuple.Direction dir = UP;
    private int t = 3;
    private int tc = 4;
    private int tr = 5;

    @Before
    public void setUp() throws Exception {
        tuple = FlexTuple.create(id, deltaP, dir, t, tr, tc);

    }

    @Test
    public void testCreation() {
        assertEquals(deltaP, tuple.getDeltaP(), 0);
        assertEquals(id, tuple.getId(), 0);
        assertEquals(t, tuple.getT(), 0);
        assertEquals(tr, tuple.getTR(), 0);
        assertEquals(tc, tuple.getTC(), 0);
        assertEquals(dir, tuple.getDirection());

    }

    @Test
    public void testDuplicateAndHash() {
        FlexTuple tuple2 = FlexTuple.create(id, deltaP, dir, t, tr, tc);
        FlexTuple tuple3 = FlexTuple.create(id, deltaP, dir, t, tr + 5, tc);
        FlexTuple tuple4 = FlexTuple.create(id + 1, deltaP, dir, t, tr, tc);
        FlexTuple tuple5 = FlexTuple.create(id, deltaP + 2, dir, t, tr, tc);
        FlexTuple tuple6 = FlexTuple.create(id, deltaP, FlexTuple.Direction.DOWN, t, tr, tc);
        FlexTuple tuple7 = FlexTuple.create(id, deltaP, dir, t, tr, tc - 2);
        Object tuple8 = mock(FlexTuple.class);
        FlexTuple tuple9 = FlexTuple.create(id, deltaP, dir, t + 3, tr, tc);

        assertEquals(tuple2, tuple);
        assertNotEquals(tuple3, tuple);
        assertNotEquals(tuple4, tuple);
        assertNotEquals(tuple5, tuple);
        assertNotEquals(tuple6, tuple);
        assertNotEquals(tuple7, tuple);
        assertNotEquals(tuple8, tuple);
        assertNotEquals(tuple9, tuple);
        assertNotEquals(tuple, null);
        assertEquals(tuple, tuple);
        assertNotEquals(tuple, new Integer(5));

        assertEquals(tuple2.hashCode(), tuple.hashCode());
        assertNotEquals(tuple6.hashCode(), tuple.hashCode());

    }

    @Test
    public void testToString() {
        FlexTuple tuple2 = FlexTuple.create(id, deltaP, dir, t, tr, tc);
        FlexTuple tuple3 = FlexTuple.create(id, deltaP, dir, t, tr + 5, tc);
        FlexTuple tuple4 = FlexTuple.create(id + 1, deltaP, dir, t, tr, tc);
        StringBuilder b = new StringBuilder();
        b.append(
                "FlexTuple{id=%s, deltaP=%s, direction=%s, t=%s, TR=%s, TC=%s}");
        Formatter formatter = new Formatter();
        formatter.format(b.toString(), id, deltaP, dir, t, tr, tc);
        assertEquals( formatter.toString(),tuple.toString());
        formatter.close();
    }

}
