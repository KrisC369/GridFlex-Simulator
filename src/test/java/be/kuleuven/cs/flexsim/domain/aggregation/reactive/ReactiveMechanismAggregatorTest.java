package be.kuleuven.cs.flexsim.domain.aggregation.reactive;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.aggregation.reactive.ReactiveMechanismAggregator;
import be.kuleuven.cs.flexsim.domain.energy.tso.contractual.BalancingTSO;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.util.data.PowerCapabilityBand;

import com.google.common.collect.Lists;

public class ReactiveMechanismAggregatorTest {
    private BalancingTSO tso = mock(BalancingTSO.class);
    private ReactiveMechanismAggregator agg = new ReactiveMechanismAggregator(
            tso);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSum() {
        Site s1 = mock(Site.class);
        when(s1.getFlexTuples()).thenReturn(
                Lists.newArrayList(FlexTuple.create(1, 50, true, 0, 0, 0),
                        FlexTuple.create(1, 60, true, 0, 0, 0),
                        FlexTuple.create(1, 20, true, 0, 0, 0),
                        FlexTuple.create(1, 50, false, 0, 0, 0),
                        FlexTuple.create(1, 20, false, 0, 0, 0)));
        Site s2 = mock(Site.class);
        when(s2.getFlexTuples()).thenReturn(
                Lists.newArrayList(FlexTuple.create(1, 50, true, 0, 0, 0),
                        FlexTuple.create(1, 60, true, 0, 0, 0),
                        FlexTuple.create(1, 20, true, 0, 0, 0),
                        FlexTuple.create(1, 50, false, 0, 0, 0),
                        FlexTuple.create(1, 20, false, 0, 0, 0)));
        agg.registerClient(s1);
        agg.registerClient(s2);
        agg.tick(0);
        agg.tick(1);

        PowerCapabilityBand arg = agg.getPowerCapacity();
        // ArgumentCaptor<PowerCapabilityBand> argument = ArgumentCaptor
        // .forClass(PowerCapabilityBand.class);
        // verify(tso, times(1)).signalNewLimits(eq(agg), argument.capture());
        assertEquals(120, arg.getUp());
        assertEquals(100, arg.getDown());
    }

    @Test
    public void testRegisterWithTSO() {
        verify(tso).registerParticipant(agg);
    }
}
