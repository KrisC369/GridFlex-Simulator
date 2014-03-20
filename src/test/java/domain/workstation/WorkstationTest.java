package domain.workstation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import simulation.ISimulationContext;
import simulation.Simulator;
import domain.resource.IResource;
import domain.resource.ResourceFactory;
import domain.util.Buffer;

public class WorkstationTest {

    private Buffer<IResource> in = mock(Buffer.class);
    private Buffer<IResource> out = mock(Buffer.class);
    private Workstation w = mock(Workstation.class);
    private ISimulationContext sim = mock(ISimulationContext.class);
    private IWorkstation iew = mock(IWorkstation.class);

    @Before
    public void setUp() throws Exception {
        in = new Buffer<IResource>();
        out = new Buffer<IResource>();
        w = new Workstation(in, out, 0, 0);
        sim = mock(Simulator.class);
        sim.register(w);
        iew = Workstation.createConsuming(in, out, 1, 3);
    }

    @Test
    public void testInitial() {
        initialStateTest(w);
    }

    private void initialStateTest(IWorkstation w) {
        assertEquals(0, w.getProcessedItemsCount());
        assertTrue(w.isIdle());
    }

    @Test
    public void testFactoryMethodInitial() {
        IWorkstation iw = Workstation.createDefault(new Buffer<IResource>(),
                new Buffer<IResource>());
        assertTrue(iw.isIdle());
    }

    @Test
    public void testProcessResourceSingleSteps() {
        IResource res = pushResource(3);
        w.tick();
        testStateAfterProces1(res);
        w.tick();
        assertFalse(w.isIdle());
        multiTick(w, 3);
        testStateAfterFinalPush(res);
    }

    @Test
    public void testProcessResourceWithLongerSteps() {
        IResource res = pushResource(3);
        w.tick();
        testStateAfterProces1(res);
        w.tick();
        assertFalse(w.isIdle());
        multiTick(w, 3);
        testStateAfterFinalPush(res);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleSetResource() {
        IResource res = pushResource(1);
        w.tick();
        testStateAfterProces1(res);
        w.changeCurrentResource(res);
    }

    private IResource pushResource(int procTime) {
        IResource res = ResourceFactory.createResource(procTime);
        in.push(res);
        return res;
    }

    private void multiTick(IWorkstation s, int times) {
        for (; times > 0; times--) {
            s.tick();
        }
    }

    private void testStateAfterFinalPush(IResource res) {
        assertNull(w.getCurrentResource().orNull());
        assertTrue(in.isEmpty());
        assertTrue(w.isIdle());
        assertFalse(out.isEmpty());
        assertEquals(res, out.pull());
        assertEquals(1,w.getProcessedItemsCount());
    }

    private void testStateAfterProces1(IResource res) {
        assertTrue(in.isEmpty());
        assertFalse(w.isIdle());
        assertTrue(out.isEmpty());
        assertEquals(res, w.getCurrentResource().get());
        assertEquals(0,w.getProcessedItemsCount());
    }

    // Test for consumptions.

    @Test
    public void testDefaultTotalConsumptionOfZero() {
        pushResource(1);
        multiTick(w, 5);
        assertEquals(0, w.getTotalConsumption());
    }

    @Test
    public void testDefaultOneTickConsumptionOfZero() {
        pushResource(1);
        for (int i = 0; i < 5; i++) {
            w.tick();
            assertEquals(0, w.getLastStepConsumption());
        }
    }

    @Test
    public void testConsumingTotalConsumptionOf5AfterInAndOut() {
        pushResource(1);
        multiTick(iew, 3);
        assertEquals(5, iew.getTotalConsumption()); // 1:in + 1:out + 1*3:cons
        assertEquals(1,iew.getProcessedItemsCount());
    }

    @Test
    public void testConsumingTotalConsumptionOf11AfterInAndOut() {
        pushResource(3);
        multiTick(iew, 5);
        assertEquals(11, iew.getTotalConsumption());// 1:in + 1:out + 3*3:cons
        assertEquals(1,iew.getProcessedItemsCount());
    }

    @Test
    public void testConsumingOneTickConsumptionOf3() {
        pushResource(1);
        iew.tick();
        assertEquals(1, iew.getLastStepConsumption());
        iew.tick();
        assertEquals(3, iew.getLastStepConsumption());
        iew.tick();
        assertEquals(1, iew.getLastStepConsumption());
        assertEquals(1,iew.getProcessedItemsCount());
    }

}
