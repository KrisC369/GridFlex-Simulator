package be.kuleuven.cs.flexsim.domain.aggregation.brp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.energy.tso.contractual.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.site.Site;

public class BRPAggregatorTest {

    private static final long _400 = 400;
    private BRPAggregator agg = mock(BRPAggregator.class);
    private Site s = mock(Site.class);

    @Before
    public void setUp() throws Exception {
        agg = new BRPAggregator(mock(BalancingSignal.class), 0.5, 0.5);
    }

    @Test
    public void testRegisterPaymMediator() {
        agg = new BRPAggregator(mock(BalancingSignal.class), 0.5, 0.5);
        agg.registerClient(s);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(s);
        assertNotNull(t);
        assertTrue(agg.getClients().contains(s));
        assertTrue(t.getTarget().equals(s));
    }

    @Test
    public void testSetBudget() {
        agg = new BRPAggregator(mock(BalancingSignal.class), 0.5, 0.5);
        agg.registerClient(s);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(s);
        t.setBudget(_400);
        assertEquals(_400, t.getCurrentBudget());
    }

}
