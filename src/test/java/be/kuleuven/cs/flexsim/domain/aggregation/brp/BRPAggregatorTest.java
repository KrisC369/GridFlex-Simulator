package be.kuleuven.cs.flexsim.domain.aggregation.brp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.energy.tso.contractual.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteSimulation;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class BRPAggregatorTest {

    private static final long _400 = 400;
    private BRPAggregator agg = mock(BRPAggregator.class);
    private Site site = mock(Site.class);
    private Simulator sim = Simulator.createSimulator(1);

    @Before
    public void setUp() throws Exception {
        agg = new BRPAggregator(mock(BalancingSignal.class), 0.5, 0.5);
        site = SiteSimulation.createDefault(0, 0, 0, 2);
        sim = Simulator.createSimulator(1);
    }

    @Test
    public void testRegisterPaymMediator() {
        agg = new BRPAggregator(mock(BalancingSignal.class), 0.5, 0.5);
        agg.registerClient(site);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site);
        assertNotNull(t);
        assertTrue(agg.getClients().contains(site));
        assertTrue(t.getTarget().equals(site));
    }

    @Test
    public void testSetBudget() {
        agg = new BRPAggregator(mock(BalancingSignal.class), 0.5, 0.5);
        agg.registerClient(site);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site);
        t.setBudget(_400);
        assertEquals(_400, t.getCurrentBudget());
    }

    @Test
    public void testDoReservationPayment() {
        agg = new BRPAggregator(mock(BalancingSignal.class), 0.5, 0.5);
        agg.registerClient(site);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site);
        sim.register(t);
        t.setBudget(_400);
        double proportion = 0.3;
        t.registerReservation(proportion);
        assertEquals(_400 * proportion * 0.5, t.getTotalProfit(), 0.01);
        sim.start();
    }

    @Test
    public void testDoActivationPayment() {
        agg = new BRPAggregator(mock(BalancingSignal.class), 0.5, 0.5);
        agg.registerClient(site);
        RenumerationMediator t = agg.getActualPaymentMediatorFor(site);
        sim.register(t);
        t.setBudget(_400);
        double proportion = 0.3;
        t.registerActivation(proportion);
        assertEquals(_400 * proportion * 0.5, t.getTotalProfit(), 0.01);
        sim.start();
    }
}
