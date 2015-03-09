package be.kuleuven.cs.flexsim.domain.energy.tso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.tso.simple.CopperplateTSO;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class CopperPlateTSOTest {
    private Site siteMock = mock(Site.class);
    private CopperplateTSO tso = mock(CopperplateTSO.class);
    private EnergyProductionTrackable out = mock(EnergyProductionTrackable.class);

    @Before
    public void setUp() throws Exception {
        siteMock = mock(Site.class);
        out = mock(EnergyProductionTrackable.class);
        Mockito.when(out.getLastStepProduction()).thenReturn(20.0);
        Mockito.when(siteMock.getLastStepConsumption()).thenReturn(20.0);
        Mockito.when(siteMock.getTotalConsumption()).thenReturn(20.0);
        tso = new CopperplateTSO(siteMock);
        tso.registerProducer(out);
    }

    @Test
    public void testInit() {
        assertTrue(tso.getSimulationSubComponents().contains(siteMock));

    }

    @Test
    public void testCopperPlateZeroBalanceReturn() {
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(0, tso.getCurrentImbalance(), 0);
    }

    @Test
    public void testCopperPlateNonZeroBalanceReturn() {
        out = mock(EnergyProductionTrackable.class);
        Mockito.when(out.getLastStepProduction()).thenReturn(10.0);
        Mockito.when(siteMock.getLastStepConsumption()).thenReturn(40.0);
        tso = new CopperplateTSO(siteMock);
        tso.registerProducer(out);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-30, tso.getCurrentImbalance(), 0);
    }

    @Test
    public void testCopperPlateAfterAggReturn() {
        out = mock(EnergyProductionTrackable.class);
        Mockito.when(out.getLastStepProduction()).thenReturn(50.0);
        Mockito.when(siteMock.getLastStepConsumption()).thenReturn(80.0);
        tso = new CopperplateTSO(siteMock);
        tso.registerProducer(out);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-30, tso.getCurrentImbalance(), 0);
        Mockito.when(siteMock.getLastStepConsumption()).thenReturn(0.0);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(50, tso.getCurrentImbalance(), 0);
        Mockito.when(out.getLastStepProduction()).thenReturn(0.0);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(0, tso.getCurrentImbalance(), 0);
    }

    @Test
    public void testRegisterAndObserver() {
        int initialbal = 20;
        final int result = 253;
        out = mock(EnergyProductionTrackable.class);
        Mockito.when(out.getLastStepProduction()).thenReturn(253.0);
        tso = new CopperplateTSO();
        tso.registerProducer(out);

        Simulator s = Simulator.createSimulator(20);
        Listener<Integer> mockListener = mock(Listener.class);
        tso.addNewBalanceValueListener(mockListener);
        s.register(tso);
        tso.tick(1);
        tso.afterTick(1);
        int answer = tso.getCurrentImbalance();
        verify(mockListener, times(1)).eventOccurred(result);
        assertEquals(answer, result, 0);
    }
}
