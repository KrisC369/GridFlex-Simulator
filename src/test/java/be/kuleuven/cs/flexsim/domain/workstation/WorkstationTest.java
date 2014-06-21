package be.kuleuven.cs.flexsim.domain.workstation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;

public class WorkstationTest {

    private Buffer<Resource> in = new Buffer<Resource>();
    private Buffer<Resource> out = new Buffer<Resource>();
    private WorkstationImpl wSingle = mock(WorkstationImpl.class);
    private SimulationContext sim = mock(SimulationContext.class);
    private Workstation iew = mock(Workstation.class);
    private static final double DELTA = 0.05;

    @Before
    public void setUp() throws Exception {
        in = new Buffer<Resource>();
        out = new Buffer<Resource>();
        wSingle = new WorkstationImpl(in, out, 0, 0, 1,
                ConsumptionModel.CONSTANT);
        sim = mock(SimulationContext.class);
        sim.register(wSingle);
        iew = WorkstationFactory.createConsuming(in, out, 1, 3);
    }

    @Test
    public void testConsumingOneTickConsumptionOf3() {
        pushResource(1);
        iew.tick(0);
        assertEquals(1, iew.getLastStepConsumption(), DELTA);
        iew.tick(0);
        assertEquals(3, iew.getLastStepConsumption(), DELTA);
        iew.tick(0);
        assertEquals(0, iew.getLastStepConsumption(), DELTA);
        assertEquals(1, iew.getProcessedItemsCount());
    }

    @Test
    public void testConsumingTotalConsumptionOf11AfterInAndOut() {
        pushResource(3);
        multiTick(iew, 5);
        assertEquals(11, iew.getTotalConsumption(), DELTA);// 1:in + 1:out +
                                                           // 3*3:cons
        assertEquals(1, iew.getProcessedItemsCount());
    }

    @Test
    public void testConsumingTotalConsumptionOf5AfterInAndOut() {
        pushResource(1);
        multiTick(iew, 3);
        assertEquals(5, iew.getTotalConsumption(), DELTA); // 1:in + 1:out +
                                                           // 1*3:cons
        assertEquals(1, iew.getProcessedItemsCount());
    }

    @Test
    public void testOnlyFixedCostWhenFinished() {
        double r = iew.getTotalConsumption();
        int fixedCost = 1;
        assertEquals(0, r, DELTA);
        pushResource(1);

        iew.tick(0);
        assertNotEquals(r, iew.getTotalConsumption());
        r = iew.getTotalConsumption();
        iew.tick(0);
        assertNotEquals(r, iew.getTotalConsumption());
        r = iew.getTotalConsumption();
        iew.tick(0);
        assertEquals(r + 1, iew.getTotalConsumption(), 0.01);
        r = iew.getTotalConsumption();
        iew.tick(0);
        assertEquals(r + 1, iew.getTotalConsumption(), 0.01);
        assertEquals(r, iew.getTotalConsumption(), fixedCost);
        r = iew.getTotalConsumption();
        iew.tick(0);
        assertEquals(r + 1, iew.getTotalConsumption(), 0.01);
        assertEquals(r, iew.getTotalConsumption(), fixedCost);
        r = iew.getTotalConsumption();

    }

    @Test
    public void testDefaultOneTickConsumptionOfZero() {
        pushResource(1);
        for (int i = 0; i < 5; i++) {
            wSingle.tick(0);
            assertEquals(0, wSingle.getLastStepConsumption(), DELTA);
        }
    }

    @Test
    public void testDefaultTotalConsumptionOfZero() {
        pushResource(1);
        multiTick(wSingle, 5);
        assertEquals(0, wSingle.getTotalConsumption(), DELTA);
    }

    // @Test(expected = IllegalStateException.class)
    // public void testDoubleSetResource() {
    // Resource res = pushResource(1);
    // wSingle.tick();
    // testStateAfterProces1(res);
    // wSingle.changeCurrentResource(res);
    // }

    @Test
    public void testFactoryMethodInitial() {
        Workstation iw = WorkstationFactory.createDefault(
                new Buffer<Resource>(), new Buffer<Resource>());
        assertTrue(iw.isIdle());
    }

    @Test
    public void testInitial() {
        initialStateTest(wSingle);
    }

    @Test
    public void testProcessResourceSingleSteps() {
        Resource res = pushResource(3);
        wSingle.tick(0);
        testStateAfterProces1(res);
        wSingle.tick(0);
        assertFalse(wSingle.isIdle());
        multiTick(wSingle, 3);
        testStateAfterFinalPush(res);
    }

    @Test
    public void testProcessResourceWithLongerSteps() {
        Resource res = pushResource(3);
        wSingle.tick(0);
        testStateAfterProces1(res);
        wSingle.tick(0);
        assertFalse(wSingle.isIdle());
        multiTick(wSingle, 3);
        testStateAfterFinalPush(res);
    }

    @Test
    public void testShiftableWorkstation() {
        int shift = 1;
        iew = WorkstationFactory.createShiftableWorkstation(in, out, 0, 0,
                shift);
        Resource res = pushResource(3);
        iew.tick(0);
        iew.afterTick(0);
        assertTrue(iew.isIdle());
        iew.tick(0);
        iew.afterTick(0);
        assertTrue(in.isEmpty());
        assertFalse(iew.isIdle());
        assertTrue(out.isEmpty());
        assertEquals(0, iew.getProcessedItemsCount());
        iew.tick(0);
        iew.afterTick(0);
        multiTick(iew, 3);
        assertTrue(in.isEmpty());
        assertTrue(iew.isIdle());
        assertFalse(out.isEmpty());
        assertEquals(res, out.pull());
        assertEquals(1, iew.getProcessedItemsCount());
    }

    @Test
    public void testDelayedStartDecorator() {
        int shift = 1;
        Workstation mock = mock(Workstation.class);
        Workstation deco = new DelayedStartStationDecorator(shift, mock);
        deco.afterTick(0);
        verify(mock, times(0)).afterTick(0);
        deco.afterTick(0);
        verify(mock, times(1)).afterTick(0);

        deco.tick(0);
        verify(mock, times(0)).tick(0);
        deco.tick(0);
        verify(mock, times(1)).tick(0);

        SimulationContext c = mock(SimulationContext.class);
        deco.initialize(c);
        verify(mock, times(1)).initialize(c);

        deco.getLastStepConsumption();
        verify(mock, times(1)).getLastStepConsumption();

        deco.getProcessedItemsCount();
        verify(mock, times(1)).getProcessedItemsCount();

        deco.getTotalConsumption();
        verify(mock, times(1)).getTotalConsumption();

        deco.isIdle();
        verify(mock, times(1)).isIdle();
    }

    @Test
    public void testDecoratorCombinationsOnCreation() {
        Workstation mock = mock(Workstation.class);
        Workstation deco = new CurtailableStationDecorator(mock);

        assertTrue(deco instanceof CurtailableWorkstation);
        assertFalse(deco instanceof SteerableWorkstation);
        deco = new SteerableCurtailableStationDecorator(
                mock(SteerableStationImpl.class));
        assertTrue(deco instanceof CurtailableWorkstation);
        assertTrue(deco instanceof SteerableWorkstation);
    }

    @Test
    public void testCurtailableStationProcessingAndConsumption() {
        Workstation curt = WorkstationFactory.createCurtailableStation(in, out,
                1, 3, 0);
        CurtailableWorkstation curt2 = ((CurtailableWorkstation) curt);

        Resource res = pushResource(3);
        multiTick(curt, 2);
        assertTrue(in.isEmpty());
        assertFalse(curt.isIdle());
        assertTrue(out.isEmpty());
        assertEquals(0, curt.getProcessedItemsCount());
        assertNotEquals(0, curt.getLastStepConsumption());
        double r = curt.getTotalConsumption();
        curt2.doFullCurtailment();

        multiTick(curt, 20);
        curt.afterTick(0);

        assertTrue(in.isEmpty());
        assertFalse(curt.isIdle());
        assertTrue(out.isEmpty());
        assertEquals(0, curt.getProcessedItemsCount());
        assertEquals(0, curt.getLastStepConsumption(), 0.05);
        assertEquals(r, curt.getTotalConsumption(), DELTA);

        curt2.restore();
        multiTick(curt, 3);

        assertTrue(in.isEmpty());
        assertTrue(curt.isIdle());
        assertFalse(out.isEmpty());
        assertEquals(res, out.pull());
        assertEquals(1, curt.getProcessedItemsCount());
    }

    @Test
    public void testSteerableCurtailableStationProcessingAndConsumption() {
        Workstation curt = WorkstationFactory.createMultiCapConsuming(in, out,
                1, 3, 1);
        CurtailableWorkstation curt2 = ((CurtailableWorkstation) curt);

        Resource res = pushResource(3);
        multiTick(curt, 2);
        assertTrue(in.isEmpty());
        assertFalse(curt.isIdle());
        assertTrue(out.isEmpty());
        assertEquals(0, curt.getProcessedItemsCount());
        assertNotEquals(0, curt.getLastStepConsumption());
        double r = curt.getTotalConsumption();
        curt2.doFullCurtailment();

        multiTick(curt, 20);
        curt.afterTick(0);
        assertTrue(curt2.isCurtailed());
        curt2.restore();
        multiTick(curt, 3);

        assertTrue(in.isEmpty());
        assertTrue(curt.isIdle());
        assertFalse(out.isEmpty());
        assertEquals(res, out.pull());
        assertEquals(1, curt.getProcessedItemsCount());
    }

    @Test
    public void testSteerableProcessingAndConsumption() {
        int defWorkingCons = 3;
        int newWorkingCons = 500;
        Workstation curt = WorkstationFactory.createMultiCapLinearConsuming(in,
                out, 1, 502, 1);
        SteerableWorkstation steer2 = ((SteerableWorkstation) curt);

        Resource res = pushResource(20);
        multiTick(curt, 2);
        assertTrue(in.isEmpty());
        assertFalse(curt.isIdle());
        assertTrue(out.isEmpty());
        assertEquals(0, curt.getProcessedItemsCount());
        assertNotEquals(0, curt.getLastStepConsumption());
        assertEquals(25, curt.getLastStepConsumption(), 2);

        steer2.favorSpeedOverFixedEConsumption(newWorkingCons, 10);

        multiTick(curt, 1);
        assertEquals(newWorkingCons, curt.getLastStepConsumption(), 2);
        multiTick(curt, 6);

        assertTrue(in.isEmpty());
        assertTrue(curt.isIdle());
        assertFalse(out.isEmpty());
        assertEquals(res, out.pull());
        assertEquals(1, curt.getProcessedItemsCount());

        steer2.favorFixedEConsumptionOverSpeed(newWorkingCons, 10);
        res = pushResource(20);
        multiTick(curt, 10);
        assertTrue(in.isEmpty());
        assertFalse(curt.isIdle());
        assertTrue(out.isEmpty());
        assertEquals(1, curt.getProcessedItemsCount());
        assertNotEquals(0, curt.getLastStepConsumption());
        assertEquals(newWorkingCons / 2, curt.getLastStepConsumption(), 50);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleCurtailment() {
        Workstation curt = WorkstationFactory.createCurtailableStation(in, out,
                0, 0, 0);
        CurtailableWorkstation curt2 = ((CurtailableWorkstation) curt);

        curt2.doFullCurtailment();
        assertTrue(curt2.isCurtailed());
        curt2.doFullCurtailment();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleRestore() {
        Workstation curt = WorkstationFactory.createCurtailableStation(in, out,
                0, 0, 0);
        CurtailableWorkstation curt2 = ((CurtailableWorkstation) curt);

        curt2.doFullCurtailment();
        assertTrue(curt2.isCurtailed());
        curt2.restore();
        assertFalse(curt2.isCurtailed());
        curt2.restore();
    }

    @Test
    public void testMultiCapacityHigherBufferResourceHandling() {
        iew = WorkstationFactory.createMultiCapConsuming(in, out, 1, 3, 3);
        multiPushResource(6, 1);
        multiTick(iew, 1);
        assertEquals(3, in.getCurrentOccupancyLevel());
        multiTick(iew, 2);
        assertEquals(3, iew.getProcessedItemsCount());
        multiTick(iew, 1);
        assertEquals(0, in.getCurrentOccupancyLevel());
        multiTick(iew, 1);
        assertEquals(6, iew.getProcessedItemsCount());
    }

    @Test
    public void testMultiCapacityLowerBufferResourceHandling() {
        iew = WorkstationFactory.createMultiCapConsuming(in, out, 1, 3, 12);
        multiPushResource(6, 1);
        multiTick(iew, 1);
        assertEquals(0, in.getCurrentOccupancyLevel());
        multiTick(iew, 2);
        assertEquals(6, iew.getProcessedItemsCount());
        multiTick(iew, 1);
        assertEquals(0, in.getCurrentOccupancyLevel());
        multiTick(iew, 1);
        assertEquals(6, iew.getProcessedItemsCount());
    }

    @Test
    public void testMultiCapacityLinearConsumption() {
        iew = WorkstationFactory.createMultiCapLinearConsuming(in, out, 1, 6,
                12);
        multiPushResource(24, 10);
        multiTick(iew, 1);
        assertEquals(1, iew.getLastStepConsumption(), 0.05);
        multiTick(iew, 1);
        assertEquals(1.5, iew.getLastStepConsumption(), 0.05);
        multiTick(iew, 1);
        assertEquals(2.0, iew.getLastStepConsumption(), 0.05);
        multiTick(iew, 1);
        assertEquals(2.5, iew.getLastStepConsumption(), 0.05);
        multiTick(iew, 7);
        assertEquals(6, iew.getLastStepConsumption(), 0.05);
    }

    @Test
    public void testMultiCapacityExponentialConsumption() {
        iew = WorkstationFactory.createMultiCapExponentialConsuming(in, out, 1,
                7, 12);
        multiPushResource(24, 10);
        multiTick(iew, 1);
        assertEquals(1, iew.getLastStepConsumption(), 0.001);
        multiTick(iew, 1);
        assertEquals(2.196, iew.getLastStepConsumption(), 0.001);
        multiTick(iew, 1);
        assertEquals(2.431, iew.getLastStepConsumption(), 0.001);
        multiTick(iew, 1);
        assertEquals(2.712, iew.getLastStepConsumption(), 0.001);
        multiTick(iew, 1);
        assertEquals(3.0477, iew.getLastStepConsumption(), 0.001);
        multiTick(iew, 6);
        assertEquals(7, iew.getLastStepConsumption(), 0.001);

    }

    private void multiPushResource(int n, int k) {
        for (int i = 0; i < n; i++) {
            pushResource(k);
        }
    }

    private void initialStateTest(Workstation w) {
        assertEquals(0, w.getProcessedItemsCount());
        assertTrue(w.isIdle());
    }

    private void multiTick(Workstation s, int times) {
        for (; times > 0; times--) {
            s.tick(0);
            s.afterTick(0);
        }
    }

    private Resource pushResource(int procTime) {
        Resource res = ResourceFactory.createResource(procTime);
        in.push(res);
        return res;
    }

    private void testStateAfterFinalPush(Resource res) {
        assertTrue(wSingle.getCurrentResources().isEmpty());
        assertTrue(in.isEmpty());
        assertTrue(wSingle.isIdle());
        assertFalse(out.isEmpty());
        assertEquals(res, out.pull());
        assertEquals(1, wSingle.getProcessedItemsCount());
    }

    private void testStateAfterProces1(Resource res) {
        assertTrue(in.isEmpty());
        assertFalse(wSingle.isIdle());
        assertTrue(out.isEmpty());
        assertFalse(wSingle.getCurrentResources().isEmpty());
        assertEquals(0, wSingle.getProcessedItemsCount());
    }

}
