package be.kuleuven.cs.flexsim.domain.workstation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;
import be.kuleuven.cs.flexsim.domain.workstation.WorkstationImpl;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class WorkstationTest {

    private Buffer<Resource> in = mock(Buffer.class);
    private Buffer<Resource> out = mock(Buffer.class);
    private WorkstationImpl w = mock(WorkstationImpl.class);
    private SimulationContext sim = mock(SimulationContext.class);
    private Workstation iew = mock(Workstation.class);

    @Before
    public void setUp() throws Exception {
        in = new Buffer<Resource>();
        out = new Buffer<Resource>();
        w = new WorkstationImpl(in, out, 0, 0);
        sim = mock(Simulator.class);
        sim.register(w);
        iew = WorkstationImpl.createConsuming(in, out, 1, 3);
    }

    @Test
    public void testConsumingOneTickConsumptionOf3() {
        pushResource(1);
        iew.tick();
        assertEquals(1, iew.getLastStepConsumption());
        iew.tick();
        assertEquals(3, iew.getLastStepConsumption());
        iew.tick();
        assertEquals(0, iew.getLastStepConsumption());
        assertEquals(1, iew.getProcessedItemsCount());
    }

    @Test
    public void testConsumingTotalConsumptionOf11AfterInAndOut() {
        pushResource(3);
        multiTick(iew, 5);
        assertEquals(11, iew.getTotalConsumption());// 1:in + 1:out + 3*3:cons
        assertEquals(1, iew.getProcessedItemsCount());
    }

    @Test
    public void testConsumingTotalConsumptionOf5AfterInAndOut() {
        pushResource(1);
        multiTick(iew, 3);
        assertEquals(5, iew.getTotalConsumption()); // 1:in + 1:out + 1*3:cons
        assertEquals(1, iew.getProcessedItemsCount());
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
    public void testDefaultTotalConsumptionOfZero() {
        pushResource(1);
        multiTick(w, 5);
        assertEquals(0, w.getTotalConsumption());
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleSetResource() {
        Resource res = pushResource(1);
        w.tick();
        testStateAfterProces1(res);
        w.changeCurrentResource(res);
    }

    @Test
    public void testFactoryMethodInitial() {
        Workstation iw = WorkstationImpl.createDefault(new Buffer<Resource>(),
                new Buffer<Resource>());
        assertTrue(iw.isIdle());
    }

    @Test
    public void testInitial() {
        initialStateTest(w);
    }

    @Test
    public void testProcessResourceSingleSteps() {
        Resource res = pushResource(3);
        w.tick();
        testStateAfterProces1(res);
        w.tick();
        assertFalse(w.isIdle());
        multiTick(w, 3);
        testStateAfterFinalPush(res);
    }

    @Test
    public void testProcessResourceWithLongerSteps() {
        Resource res = pushResource(3);
        w.tick();
        testStateAfterProces1(res);
        w.tick();
        assertFalse(w.isIdle());
        multiTick(w, 3);
        testStateAfterFinalPush(res);
    }

    // Test for consumptions.

    private void initialStateTest(Workstation w) {
        assertEquals(0, w.getProcessedItemsCount());
        assertTrue(w.isIdle());
    }

    private void multiTick(Workstation s, int times) {
        for (; times > 0; times--) {
            s.tick();
        }
    }

    private Resource pushResource(int procTime) {
        Resource res = ResourceFactory.createResource(procTime);
        in.push(res);
        return res;
    }

    private void testStateAfterFinalPush(Resource res) {
        assertNull(w.getCurrentResource().orNull());
        assertTrue(in.isEmpty());
        assertTrue(w.isIdle());
        assertFalse(out.isEmpty());
        assertEquals(res, out.pull());
        assertEquals(1, w.getProcessedItemsCount());
    }

    private void testStateAfterProces1(Resource res) {
        assertTrue(in.isEmpty());
        assertFalse(w.isIdle());
        assertTrue(out.isEmpty());
        assertEquals(res, w.getCurrentResource().get());
        assertEquals(0, w.getProcessedItemsCount());
    }

}
