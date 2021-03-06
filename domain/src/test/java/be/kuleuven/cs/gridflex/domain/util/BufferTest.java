package be.kuleuven.cs.gridflex.domain.util;

import be.kuleuven.cs.gridflex.domain.resource.Resource;
import be.kuleuven.cs.gridflex.domain.resource.ResourceFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BufferTest {

    @SuppressWarnings("null")
    private Buffer<Resource> b = new Buffer<Resource>();
    @SuppressWarnings("null")
    private Resource res = mock(Resource.class);

    @Before
    public void setUp() throws Exception {
        b = new Buffer<Resource>();
        res = mock(Resource.class);
    }

    @Test
    public void testBeenBufferedNotification() {
        b.push(res);
        verify(res, times(1)).notifyOfHasBeenBuffered();
    }

    @Test
    public void testBigNumberThroughput() {
        Buffer<Bufferable> b = new Buffer<Bufferable>();
        List<Bufferable> results = new ArrayList<Bufferable>();
        Bufferable tmp;
        int n = 101912;
        for (int i = 0; i < n; i++) {
            tmp = ResourceFactory.createResource();
            b.push(tmp);
            results.add(tmp);
        }
        for (Bufferable i : results) {
            assertEquals(i, b.pull());
        }
    }

    @Test
    public void testCorrectOrderPop() {
        Resource res2 = mock(Resource.class);
        b.push(res);
        b.push(res2);
        assertEquals(res, b.pull());
        assertEquals(res2, b.pull());
    }

    @Test
    public void testInitialise() {
        assertTrue(b.isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void testPopEmpty() {
        b.pull();
    }

    @Test
    public void testPullAll() {
        Resource res2 = mock(Resource.class);
        b.push(res);
        b.push(res2);
        Collection<Resource> returnset = b.pullAll();
        assertTrue(returnset.contains(res2));
        assertTrue(returnset.contains(res));
        assertEquals(2, returnset.size());
    }

    @Test
    public void testPush1Element() {
        b.push(res);
        assertFalse(b.isEmpty());
        assertEquals(1, b.getCurrentOccupancyLevel());
    }

    @Test
    public void testPushAll() {
        Resource res2 = mock(Resource.class);
        List<Resource> reslist = new ArrayList<>();
        reslist.add(res2);
        reslist.add(res);
        b.pushAll(reslist);
        Collection<Resource> returnset = b.pullAll();
        assertTrue(returnset.contains(res2));
        assertTrue(returnset.contains(res));
        assertEquals(2, returnset.size());
    }

    @Test
    public void testSuccesfulPop() {
        b.push(res);
        assertEquals(res, b.pull());

    }
}
