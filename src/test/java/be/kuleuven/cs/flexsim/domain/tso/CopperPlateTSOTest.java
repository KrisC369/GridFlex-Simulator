package be.kuleuven.cs.flexsim.domain.tso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import be.kuleuven.cs.flexsim.domain.site.Site;

public class CopperPlateTSOTest {
    private Site siteMock = mock(Site.class);
    private SteeringSignal ss = mock(SteeringSignal.class);
    private CopperPlateTSO tso = mock(CopperPlateTSO.class);

    @Before
    public void setUp() throws Exception {
        siteMock = mock(Site.class);
        ss = mock(SteeringSignal.class);
        Mockito.when(ss.getCurrentValue(anyInt())).thenReturn(-30);
        Mockito.when(siteMock.getAggregatedLastStepConsumptions()).thenReturn(
                20);
        Mockito.when(siteMock.getAggregatedTotalConsumptions()).thenReturn(20);
        tso = new CopperPlateTSO(ss, siteMock);
    }

    @Test
    public void testInit() {
        assertTrue(tso.getSimulationSubComponents().contains(siteMock));

    }

    @Test
    public void testCopperPlateZeroBalanceReturn() {
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-50, tso.getCurrentValue(0), 0);
    }

    @Test
    public void testCopperPlateNonZeroBalanceReturn() {
        int initialbal = 20;
        tso = new CopperPlateTSO(initialbal, ss, siteMock);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-30, tso.getCurrentValue(0), 0);
    }

    @Test
    public void testCopperPlateAfterAggReturn() {
        int initialbal = 20;
        tso = new CopperPlateTSO(initialbal, ss, siteMock);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-30, tso.getCurrentValue(0), 0);
        Mockito.when(siteMock.getAggregatedLastStepConsumptions())
                .thenReturn(0);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(-10, tso.getCurrentValue(0), 0);
        Mockito.when(ss.getCurrentValue(anyInt())).thenReturn(-20);
        tso.tick(0);
        tso.afterTick(0);
        assertEquals(0, tso.getCurrentValue(0), 0);
    }
}
