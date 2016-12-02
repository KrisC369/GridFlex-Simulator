package be.kuleuven.cs.flexsim.domain.finance;

import be.kuleuven.cs.flexsim.domain.process.ProductionLine;
import be.kuleuven.cs.flexsim.domain.process.ProductionLine.ProductionLineBuilder;
import be.kuleuven.cs.flexsim.domain.process.ProductionLineTest.ChangeEventComponent;
import be.kuleuven.cs.flexsim.domain.process.ResourceConsumptionTrackableComponent;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.site.ActivateFlexCommand;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteBuilder;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.flexsim.simulation.Simulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FinanceTrackerTest {
    private ResourceConsumptionTrackableComponent mockPL = mock(
            ResourceConsumptionTrackableComponent.class);
    private FinanceTrackerImpl t = FinanceTrackerImpl.createDefault(mockPL);
    private SimulationContext sim = mock(SimulationContext.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        t = FinanceTrackerImpl.createCustom(mockPL, RewardModel.CONSTANT,
                DebtModel.CONSTANT);
        sim = Simulator.createSimulator(20);
        sim.register(mockPL);
    }

    @Test
    public void testConstructor() {
        assertFalse(t.getSimulationSubComponents().isEmpty());

    }

    @Test
    public void signalConsumptionTest() {
        ProductionLine mockPL = new ProductionLineBuilder().addShifted(1)
                .build();
        t = FinanceTrackerImpl.createDefault(mockPL);
        int n = 3;
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        SimulationComponent m = mock(SimulationComponent.class);
        SimulationComponent tester = new ChangeEventComponent(m);
        long duration = 20;
        sim.register(t);
        sim.register(tester);
        ((Simulator) sim).start();
        verify(m, times((int) duration)).tick(0);
        assertEquals("simulation:stopped",
                ((ChangeEventComponent) tester).getLastType());
    }

    @Test
    public void getCurrentPaymentRateTest() {
        ProductionLine mockPL = new ProductionLineBuilder().addShifted(1)
                .build();
        t = FinanceTrackerImpl.createDefault(mockPL);
        int n = 3;
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        sim.register(t);
        sim.register(mockPL);

        assertEquals(0, t.getTotalCost(), 0);
        ((Simulator) sim).start();
        assertNotEquals(0, t.getTotalCost());
    }

    @Test
    public void getCurrentRewardRateTest() {
        ProductionLine mockPL = new ProductionLineBuilder().addShifted(1)
                .build();
        t = FinanceTrackerImpl.createDefault(mockPL);
        int n = 3;
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        sim.register(t);
        sim.register(mockPL);
        assertEquals(0, t.getTotalReward(), 0);
        ((Simulator) sim).start();
        assertNotEquals(0, t.getTotalReward());
        assertEquals(0, mockPL.getBufferOccupancyLevels()
                .get(mockPL.getBufferOccupancyLevels().size() - 1), 0);
    }

    @Test
    public void getProfitTest() {
        ProductionLine mockPL = new ProductionLineBuilder().addShifted(1)
                .build();
        t = FinanceTrackerImpl.createDefault(mockPL);
        int n = 3;
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        sim.register(t);
        assertEquals(0, t.getTotalReward(), 0);
        ((Simulator) sim).start();
        double reward = t.getTotalReward();
        double cost = t.getTotalCost();
        assertEquals(reward - cost, t.getTotalProfit(), 0);
    }

    @Test
    public void testAggregateSums() {
        ProductionLine mockPL = new ProductionLineBuilder().addShifted(1)
                .build();
        ProductionLine mockPL2 = new ProductionLineBuilder().addShifted(1)
                .build();
        FinanceTrackerImpl t = FinanceTrackerImpl.createDefault(mockPL);
        FinanceTrackerImpl t2 = FinanceTrackerImpl.createDefault(mockPL2);
        int n = 3;
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        res = ResourceFactory.createBulkMPResource(n, 3, 1);
        mockPL.deliverResources(res);
        FinanceTracker ta = FinanceTrackerImpl.createAggregate(t, t2);
        sim.register(ta);
        assertEquals(0, t.getTotalReward(), 0);
        ((Simulator) sim).start();
        double reward = t.getTotalReward();
        double cost = t.getTotalCost();
        double reward2 = t2.getTotalReward();
        double cost2 = t2.getTotalCost();
        double profit = t.getTotalProfit();
        double profit2 = t2.getTotalProfit();
        assertEquals(cost + cost2, ta.getTotalCost(), 1);
        assertEquals(reward + reward2, ta.getTotalReward(), 1);
        assertEquals(profit + profit2, ta.getTotalProfit(), 1);
    }

    @Test
    public void noContextTest() {
        ProductionLine mockPL = new ProductionLineBuilder().addShifted(1)
                .build();
        t = FinanceTrackerImpl.createDefault(mockPL);
        exception.expect(IllegalStateException.class);
        ((FinanceTrackerImpl) t).afterTick(1);
    }

    @Test
    public void testBalancingFee() {
        final int pay = 300;
        final int min = 50, max = 300, base = 200, tuples = 4;
        final Site s = SiteBuilder.newSiteSimulation().withBaseConsumption(base)
                .withMinConsumption(min).withMaxConsumption(max)
                .withTuples(tuples).create();

        t = (FinanceTrackerImpl) FinanceTrackerImpl.createBalancingFeeTracker(s,
                pay);

        sim = Simulator.createSimulator(1);
        sim.register(s);
        sim.register(t);
        ((Simulator) sim).start();
        final long id = s.getFlexTuples().get(0).getId();
        assertEquals(0, t.getTotalProfit(), 0);
        s.activateFlex(new ActivateFlexCommand() {

            @Override
            public long getReferenceID() {
                return id;
            }

        });
        assertEquals(pay * (max - base) / tuples, t.getTotalProfit(), 0);

    }

    @Test
    public void testBalancingFee2() {
        final int pay = 300;
        final int min = 50, max = 300, base = 200, tuples = 4;
        final Site s = SiteBuilder.newSiteSimulation().withBaseConsumption(base)
                .withMinConsumption(min).withMaxConsumption(max)
                .withTuples(tuples).create();

        t = (FinanceTrackerImpl) FinanceTrackerImpl.createBalancingFeeTracker(s,
                pay);

        sim = Simulator.createSimulator(1);
        sim.register(s);
        sim.register(t);
        ((Simulator) sim).start();
        final long id = s.getFlexTuples().get(0).getId();
        assertEquals(0, t.getTotalProfit(), 0);
        s.activateFlex(new ActivateFlexCommand() {

            @Override
            public long getReferenceID() {
                return id;
            }
        });
        assertEquals(pay * (max - base) / tuples, t.getTotalProfit(), 0);
    }
}
