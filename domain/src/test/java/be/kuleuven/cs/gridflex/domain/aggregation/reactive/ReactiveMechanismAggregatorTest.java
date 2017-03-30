package be.kuleuven.cs.gridflex.domain.aggregation.reactive;

import be.kuleuven.cs.gridflex.domain.energy.tso.contractual.BalancingTSO;
import be.kuleuven.cs.gridflex.domain.site.Site;
import be.kuleuven.cs.gridflex.domain.util.FlexTuple;
import be.kuleuven.cs.gridflex.domain.util.data.IntPowerCapabilityBand;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import static be.kuleuven.cs.gridflex.domain.util.FlexTuple.Direction.fromRepresentation;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                Lists.newArrayList(FlexTuple.create(1, 50, fromRepresentation(true), 0, 0, 0),
                        FlexTuple.create(1, 60, fromRepresentation(true), 0, 0, 0),
                        FlexTuple.create(1, 20, fromRepresentation(true), 0, 0, 0),
                        FlexTuple.create(1, 50, fromRepresentation(false), 0, 0, 0),
                        FlexTuple.create(1, 20, fromRepresentation(false), 0, 0, 0)));
        Site s2 = mock(Site.class);
        when(s2.getFlexTuples()).thenReturn(
                Lists.newArrayList(FlexTuple.create(1, 50, fromRepresentation(true), 0, 0, 0),
                        FlexTuple.create(1, 60, fromRepresentation(true), 0, 0, 0),
                        FlexTuple.create(1, 20, fromRepresentation(true), 0, 0, 0),
                        FlexTuple.create(1, 50, fromRepresentation(false), 0, 0, 0),
                        FlexTuple.create(1, 20, fromRepresentation(false), 0, 0, 0)));
        agg.registerClient(s1);
        agg.registerClient(s2);
        agg.tick(0);
        agg.tick(1);

        IntPowerCapabilityBand arg = agg.getPowerCapacity();
        // ArgumentCaptor<IntPowerCapabilityBand> argument = ArgumentCaptor
        // .forClass(IntPowerCapabilityBand.class);
        // verify(tso, times(1)).signalNewLimits(eq(agg), argument.capture());
        assertEquals(120, arg.getUp());
        assertEquals(100, arg.getDown());
    }

    @Test
    public void testRegisterWithTSO() {
        verify(tso).registerParticipant(agg);
    }
}
