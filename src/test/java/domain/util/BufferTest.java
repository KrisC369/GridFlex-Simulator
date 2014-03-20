package domain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import domain.resource.IResource;
import domain.resource.ResourceFactory;

public class BufferTest {

    @SuppressWarnings("null")
    private Buffer<IResource> b = mock(Buffer.class);
    @SuppressWarnings("null")
    private IResource res = mock(IResource.class);

    @Before
    public void setUp() throws Exception {
        b = new Buffer<IResource>();
        res = mock(IResource.class);
    }

    @Test
    public void testBeenBufferedNotification() {
        b.push(res);
        verify(res, times(1)).notifyOfHasBeenBuffered();
    }

    @Test
    public void testBigNumberThroughput() {
        Buffer<IBufferable> b = new Buffer<IBufferable>();
        List<IBufferable> results = new ArrayList<IBufferable>();
        IBufferable tmp;
        int n = 101912;
        for (int i = 0; i < n; i++) {
            tmp = ResourceFactory.createResource();
            b.push(tmp);
            results.add(tmp);
        }
        for (IBufferable i : results) {
            assertEquals(i, b.pull());
        }
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
    public void testInitialise() {
        assertTrue(b.isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void testPopEmpty() {
        b.pull();
    }

    @Test
    public void testPullAll() {
        IResource res2 = mock(IResource.class);
        b.push(res);
        b.push(res2);
        Collection<IResource> returnset = b.pullAll();
        assertTrue(returnset.contains(res2));
        assertTrue(returnset.contains(res));
        assertEquals(2, returnset.size());
    }

    @Test
    public void testPush1Element() {
        b.push(res);
        assertFalse(b.isEmpty());
        assertEquals(1, b.getCurrentCapacity());
    }

    @Test
    public void testPushAll() {
        IResource res2 = mock(IResource.class);
        List<IResource> reslist = new ArrayList<>();
        reslist.add(res2);
        reslist.add(res);
        b.pushAll(reslist);
        Collection<IResource> returnset = b.pullAll();
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
