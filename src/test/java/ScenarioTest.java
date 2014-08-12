import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregatorImpl;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTrackerImpl;
import be.kuleuven.cs.flexsim.domain.process.ProductionLine;
import be.kuleuven.cs.flexsim.domain.process.ProductionLine.ProductionLineBuilder;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteImpl;
import be.kuleuven.cs.flexsim.domain.tso.CopperPlateTSO;
import be.kuleuven.cs.flexsim.domain.tso.RandomTSO;
import be.kuleuven.cs.flexsim.domain.tso.SteeringSignal;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.Simulator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ScenarioTest {

    private Simulator s;
    private ProductionLine p;
    private FinanceTrackerImpl ft;

    @Before
    public void setUp() throws Exception {
        s = Simulator.createSimulator(5000);
        p = new ProductionLine.ProductionLineBuilder().addShifted(14)
                .addShifted(14).addShifted(4)
                .addMultiCapExponentialConsuming(4, 125).addShifted(14)
                .addShifted(14).addShifted(10).build();
        ft = FinanceTrackerImpl.createDefault(p);
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
    public void testAggregationRunStepWithCurtailmentRunner() {
        testAggregationRunStepWithCurtailment();
    }

    public double testAggregationRunStepWithCurtailment() {
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

        FinanceTrackerImpl t1 = FinanceTrackerImpl.createDefault(line1);
        FinanceTrackerImpl t2 = FinanceTrackerImpl.createDefault(line2);
        FinanceTrackerImpl t3 = FinanceTrackerImpl.createDefault(line3);
        FinanceTrackerImpl t4 = FinanceTrackerImpl.createDefault(line4);

        Simulator simulator = Simulator.createSimulator(1500);
        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);
        SteeringSignal tso = new RandomTSO(0, 1, simulator.getRandom());
        AggregatorImpl agg = new AggregatorImpl(tso, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(site1);
        simulator.register(site2);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        // System.out.println("setup 1 done");
        simulator.start();

        double profitBefore = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        // System.out.println("simulation 1 done");

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

        t1 = FinanceTrackerImpl.createDefault(line1);
        t2 = FinanceTrackerImpl.createDefault(line2);
        t3 = FinanceTrackerImpl.createDefault(line3);
        t4 = FinanceTrackerImpl.createDefault(line4);

        simulator = Simulator.createSimulator(1500);
        site1 = new SiteImpl(line1, line2);
        site2 = new SiteImpl(line3, line4);
        tso = new RandomTSO(-300, 70, simulator.getRandom());
        agg = new AggregatorImpl(tso, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(site1);
        simulator.register(site2);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        // System.out.println("Setup 2 done");
        simulator.start();
        // System.out.println("Simulation 2 done");
        double profitAfter = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        // System.out.println("Profit no curt:" + profitBefore +
        // "\nProfit Curt: "
        // + profitAfter);
        assertTrue(profitBefore < profitAfter);
        return profitAfter - profitBefore;
    }

    @Test
    public void testAggregationWithConnectedTSORunner() {
        testAggregationWithConnectedTSO();
    }

    public double testAggregationWithConnectedTSO() {
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

        FinanceTrackerImpl t1 = FinanceTrackerImpl.createDefault(line1);
        FinanceTrackerImpl t2 = FinanceTrackerImpl.createDefault(line2);
        FinanceTrackerImpl t3 = FinanceTrackerImpl.createDefault(line3);
        FinanceTrackerImpl t4 = FinanceTrackerImpl.createDefault(line4);

        Simulator simulator = Simulator.createSimulator(1500);
        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);
        SteeringSignal tso = new RandomTSO(0, 1, simulator.getRandom());
        CopperPlateTSO realTSO = new CopperPlateTSO(tso, site1, site2);
        AggregatorImpl agg = new AggregatorImpl(realTSO, 15);
        // agg.registerClient(site1);
        // agg.registerClient(site2);

        // simulator.register(agg);
        simulator.register(realTSO);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        // System.out.println("setup 1 done");
        simulator.start();

        double profitBefore = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        // System.out.println("simulation 1 done");

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

        t1 = FinanceTrackerImpl.createDefault(line1);
        t2 = FinanceTrackerImpl.createDefault(line2);
        t3 = FinanceTrackerImpl.createDefault(line3);
        t4 = FinanceTrackerImpl.createDefault(line4);

        simulator = Simulator.createSimulator(1500);
        site1 = new SiteImpl(line1, line2);
        site2 = new SiteImpl(line3, line4);
        tso = new RandomTSO(-300, 70, simulator.getRandom());
        realTSO = new CopperPlateTSO(1600, tso, site1, site2);
        agg = new AggregatorImpl(realTSO, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(realTSO);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        // System.out.println("Setup 2 done");
        simulator.start();
        // System.out.println("Simulation 2 done");
        double profitAfter = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        // System.out.println("Profit no curt:" + profitBefore +
        // "\nProfit Curt: "
        // + profitAfter);
        assertTrue(profitBefore < profitAfter);
        return profitAfter - profitBefore;
    }

    @Test
    public void testRepeatDeterminism1() {
        List<Double> results = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            results.add(testAggregationWithConnectedTSO());
        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);
    }

    @Test
    public void testRepeatDeterminismMultiThread() {
        int expsize = 15;
        final List<Double> results = Lists.newArrayList();
        for (int i = 0; i < expsize; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    results.add(testAggregationWithConnectedTSO());
                }
            }).start();
        }
        while (results.size() < expsize) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);
    }

    @Test
    public void testRepeatDeterminismMultiThread2() {
        int expsize = 15;
        final List<Double> results = Lists.newArrayList();
        for (int i = 0; i < expsize; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    results.add(testAggregationRunStepWithCurtailment());
                }
            }).start();
        }
        while (results.size() < expsize) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);
    }

    @Test
    public void testRepeatDeterminism2() {
        List<Double> results = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            results.add(testAggregationRunStepWithCurtailment());

        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);
    }
}
