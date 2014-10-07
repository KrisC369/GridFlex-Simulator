package be.kuleuven.cs.flexsim.domain.tso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.energy.tso.SimpleTSO;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.util.listener.Listener;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class CopperPlateTSOTest {
    private Site siteMock = mock(Site.class);
    private BalancingSignal ss = mock(BalancingSignal.class);
    private SimpleTSO tso = mock(SimpleTSO.class);

    @Before
    public void setUp() throws Exception {
        siteMock = mock(Site.class);
        ss = mock(BalancingSignal.class);
        Mockito.when(ss.getCurrentImbalance()).thenReturn(-30);
        Mockito.when(siteMock.getLastStepConsumption()).thenReturn(20.0);
        Mockito.when(siteMock.getTotalConsumption()).thenReturn(20.0);
        tso = new SimpleTSO(ss, siteMock);
    }

    @Test
    public void testInit() {
        assertTrue(tso.getSimulationSubComponents().contains(siteMock));

    }

    @Test
    public void testCopperPlateZeroBalanceReturn() {
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-50, tso.getCurrentImbalance(), 0);
    }

    @Test
    public void testCopperPlateNonZeroBalanceReturn() {
        int initialbal = 20;
        tso = new SimpleTSO(initialbal, ss, siteMock);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-30, tso.getCurrentImbalance(), 0);
    }

    @Test
    public void testCopperPlateAfterAggReturn() {
        int initialbal = 20;
        tso = new SimpleTSO(initialbal, ss, siteMock);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-30, tso.getCurrentImbalance(), 0);
        Mockito.when(siteMock.getLastStepConsumption()).thenReturn(0.0);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-10, tso.getCurrentImbalance(), 0);
        Mockito.when(ss.getCurrentImbalance()).thenReturn(-20);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(0, tso.getCurrentImbalance(), 0);
    }

    @Test
    public void testRegisterAndObserver() {
        int initialbal = 20;
        final int result = 253;
        tso = new SimpleTSO(initialbal, new BalancingSignal() {

            @Override
            public int getCurrentImbalance() {
                return 253;
            }

            @Override
            public void addNewBalanceValueListener(
                    Listener<? super Integer> listener) {
            }
        }, siteMock);
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

    public void addNewBalanceValueListener(Listener<? super Integer> listener) {
    }
}
