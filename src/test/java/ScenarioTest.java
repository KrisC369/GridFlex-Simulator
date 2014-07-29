import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregatorImpl;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTracker;
import be.kuleuven.cs.flexsim.domain.process.ProductionLine;
import be.kuleuven.cs.flexsim.domain.process.ProductionLine.ProductionLineBuilder;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteImpl;
import be.kuleuven.cs.flexsim.domain.tso.RandomTSO;
import be.kuleuven.cs.flexsim.domain.tso.SteeringSignal;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.Simulator;

public class ScenarioTest {

    private Simulator s;
    private ProductionLine p;
    private FinanceTracker ft;

    @Before
    public void setUp() throws Exception {
        s = Simulator.createSimulator(5000);
        p = new ProductionLine.ProductionLineBuilder().addShifted(14)
                .addShifted(14).addShifted(4)
                .addMultiCapExponentialConsuming(4, 125).addShifted(14)
                .addShifted(14).addShifted(10).build();
        ft = FinanceTracker.createDefault(p);
        s.register(p);
        s.register(ft);
    }

    @Test
    public void testSimulationRun() {
        p.deliverResources(ResourceFactory.createBulkMPResource(500000, 0, 2,
                2, 2, 2000));
        s.start();
        assertNotEquals(ft.getTotalReward(), 0);
        assertNotEquals(ft.getTotalProfit(), 0);

    }

    @Test
    public void testDoubleConfig() {
        Set<SimulationComponent> set = new HashSet<SimulationComponent>();
        set.addAll(s.getSimulationComponents());
        assertEquals(s.getSimulationComponents().size(), set.size(), 0);
    }

    @Test
    public void testAggregationRunStepWithCurtailment() {
        // Before: no curtailment.
        ProductionLine line1 = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(20)
                .addConsuming(3).addCurtailableShifted(6)
                .addCurtailableShifted(4).addConsuming(3).build();
        ProductionLine line2 = new ProductionLineBuilder()
                .setWorkingConsumption(400).setIdleConsumption(60)
                .addConsuming(3).addCurtailableShifted(6)
                .addCurtailableShifted(3).addConsuming(3).build();
        ProductionLine line3 = new ProductionLineBuilder()
                .setWorkingConsumption(600).setIdleConsumption(10)
                .addConsuming(3).addCurtailableShifted(4)
                .addCurtailableShifted(4).addConsuming(3).build();
        ProductionLine line4 = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(15)
                .addConsuming(4).addCurtailableShifted(4)
                .addCurtailableShifted(5).addConsuming(3).build();

        ResourceFactory.createBulkMPResource(50, 3, 3, 3, 3, 3, 3);

        line1.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3));
        line2.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3));
        line3.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3));
        line4.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3));

        FinanceTracker t1 = FinanceTracker.createDefault(line1);
        FinanceTracker t2 = FinanceTracker.createDefault(line2);
        FinanceTracker t3 = FinanceTracker.createDefault(line3);
        FinanceTracker t4 = FinanceTracker.createDefault(line4);

        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);
        SteeringSignal tso = new RandomTSO(-1, 0);
        AggregatorImpl agg = new AggregatorImpl(tso, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        Simulator simulator = Simulator.createSimulator(1500);
        simulator.register(agg);
        simulator.register(site1);
        simulator.register(site2);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        simulator.start();

        double profitBefore = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();

        // After: Curtailment
        line1 = new ProductionLineBuilder().setWorkingConsumption(500)
                .setIdleConsumption(20).addConsuming(3)
                .addCurtailableShifted(6).addCurtailableShifted(4)
                .addConsuming(3).build();
        line2 = new ProductionLineBuilder().setWorkingConsumption(400)
                .setIdleConsumption(60).addConsuming(3)
                .addCurtailableShifted(6).addCurtailableShifted(3)
                .addConsuming(3).build();
        line3 = new ProductionLineBuilder().setWorkingConsumption(600)
                .setIdleConsumption(10).addConsuming(3)
                .addCurtailableShifted(4).addCurtailableShifted(4)
                .addConsuming(3).build();
        line4 = new ProductionLineBuilder().setWorkingConsumption(500)
                .setIdleConsumption(15).addConsuming(4)
                .addCurtailableShifted(4).addCurtailableShifted(5)
                .addConsuming(3).build();
        ResourceFactory.createBulkMPResource(50, 3, 3, 3, 3, 3, 3);

        line1.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3));
        line2.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3));
        line3.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3));
        line4.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3));

        t1 = FinanceTracker.createDefault(line1);
        t2 = FinanceTracker.createDefault(line2);
        t3 = FinanceTracker.createDefault(line3);
        t4 = FinanceTracker.createDefault(line4);

        site1 = new SiteImpl(line1, line2);
        site2 = new SiteImpl(line3, line4);
        tso = new RandomTSO(-70, 0);
        agg = new AggregatorImpl(tso, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator = Simulator.createSimulator(1500);
        simulator.register(agg);
        simulator.register(site1);
        simulator.register(site2);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        simulator.start();

        double profitAfter = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        System.out.println("Profit no curt:" + profitBefore + "\nProfit Curt: "
                + profitAfter);
        assertTrue(profitBefore < profitAfter);
        // TODO Fix nondet.
    }
}
