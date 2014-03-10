package domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

public class BufferTest {

    private Buffer<IResource> b;
    private IResource res;

    @Before
    public void setUp() throws Exception {
        b = new Buffer<IResource>();
        res = mock(IResource.class);
    }

    @Test
    public void testInitialise() {
        assertTrue(b.isEmpty());
    }

    @Test
    public void testPush1Element() {
        b.push(res);
        assertFalse(b.isEmpty());
        assertEquals(1, b.getCurrentCapacity());
    }

    @Test(expected = NoSuchElementException.class)
    public void testPopEmpty() {
        b.pull();
    }

    @Test
    public void testSuccesfulPop() {
        b.push(res);
        assertEquals(res, b.pull());

    }

    @Test
    public void testCorrectOrderPop() {
        IResource res2 = mock(IResource.class);
        b.push(res);
        b.push(res2);
        assertEquals(res, b.pull());
        assertEquals(res2, b.pull());
    }

    @Test
    public void testBigNumberThroughput() {
        Buffer<Integer> b = new Buffer<Integer>();
        int n = 10191200;
        for (int i = 0; i < n; i++) {
            b.push(i);
        }
        for (Integer i = 0; i < n; i++) {
            assertEquals(i, b.pull());
        }
    }
}
