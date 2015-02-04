import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregationStrategyImpl;
import be.kuleuven.cs.flexsim.domain.aggregation.Aggregator;
import be.kuleuven.cs.flexsim.domain.aggregation.IndependentAggregator;
import be.kuleuven.cs.flexsim.domain.aggregation.ReactiveMechanismAggregator;
import be.kuleuven.cs.flexsim.domain.energy.generation.ConstantOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.generation.EnergyProductionTrackable;
import be.kuleuven.cs.flexsim.domain.energy.generation.RandomOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.generation.WeighedNormalRandomOutputGenerator;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingSignal;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingTSO;
import be.kuleuven.cs.flexsim.domain.energy.tso.CopperplateTSO;
import be.kuleuven.cs.flexsim.domain.energy.tso.RandomTSO;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTrackerImpl;
import be.kuleuven.cs.flexsim.domain.process.ProductionLine;
import be.kuleuven.cs.flexsim.domain.process.ProductionLine.ProductionLineBuilder;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.site.SiteImpl;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.Simulator;
import be.kuleuven.cs.gametheory.Game;
import be.kuleuven.cs.gametheory.experimentation.GameConfiguratorEx;
import be.kuleuven.cs.gametheory.experimentation.io.GameResultWriter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ScenarioTests {

    private Simulator s;
    private ProductionLine p;
    private FinanceTrackerImpl ft;
    private Logger log = LoggerFactory.getLogger("ScenarioTest.class");

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
        testAggregationRunStepWithCurtailment(1500);
    }

    public double testAggregationRunStepWithCurtailment(int simSteps) {
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

        Simulator simulator = Simulator.createSimulator(simSteps);
        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);
        BalancingSignal tso = new RandomTSO(0, 1, simulator.getRandom());
        IndependentAggregator agg = new IndependentAggregator(tso, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(site1);
        simulator.register(site2);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        log.info("Setup 1 done. Starting simulation.");
        simulator.start();

        double profitBefore = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        log.info("Simulation 1 done.");

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

        simulator = Simulator.createSimulator(simSteps);
        site1 = new SiteImpl(line1, line2);
        site2 = new SiteImpl(line3, line4);
        tso = new RandomTSO(-500, 1000, simulator.getRandom());
        agg = new IndependentAggregator(tso, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(site1);
        simulator.register(site2);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        log.info("Setup 2 done. Starting Simulation");

        simulator.start();
        log.info("Simulation 2 done.");

        double profitAfter = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        log.info("Profit no curt: {} Profit Curt: {}", profitBefore,
                profitAfter);
        assertTrue(profitBefore < profitAfter);
        return profitAfter - profitBefore;
    }

    @Test
    public void testSpecificResultRegressionWCurtAndRandomTSO() {
        // Before: no curtailment.
        int simSteps = 1500;
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

        Simulator simulator = Simulator.createSimulator(simSteps);
        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);
        BalancingSignal tso = new RandomTSO(0, 1, simulator.getRandom());
        IndependentAggregator agg = new IndependentAggregator(tso, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(site1);
        simulator.register(site2);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        log.info("Setup 1 done. Starting simulation.");
        simulator.start();

        double profitBefore = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        log.info("Simulation 1 done.");
        double expectedResult = -1.240177876E9;
        assertEquals(expectedResult, profitBefore, 0);
    }

    @Test
    public void testSpecificResultRegressionWCurtAndCopperplate() {
        // Before: no curtailment.
        int simSteps = 1500;
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

        Simulator simulator = Simulator.createSimulator(simSteps);
        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);
        EnergyProductionTrackable p1 = new WeighedNormalRandomOutputGenerator(
                -1000, 1000);
        EnergyProductionTrackable p2 = new ConstantOutputGenerator(10000);
        CopperplateTSO tso = new CopperplateTSO(site1, site2);
        tso.registerProducer(p1);
        tso.registerProducer(p2);
        IndependentAggregator agg = new IndependentAggregator(tso, 15,
                AggregationStrategyImpl.MOVINGHORIZON);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(site1);
        simulator.register(site2);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        log.info("Setup 1 done. Starting simulation.");
        simulator.start();

        double profitBefore = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        log.info("Simulation 1 done.");
        double expectedResult = -1.240177876E9;
        assertEquals(expectedResult, profitBefore, 0);
    }

    @Test
    public void testAggregationWithConnectedTSORunner() {
        testAggregationWithConnectedTSO(1500);
    }

    public double testAggregationWithConnectedTSO(int simSteps) {
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

        Simulator simulator = Simulator.createSimulator(simSteps);
        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);

        EnergyProductionTrackable p1 = new ConstantOutputGenerator(0);
        EnergyProductionTrackable p2 = new RandomOutputGenerator(0, 1);
        CopperplateTSO realTSO = new CopperplateTSO(site1, site2);
        realTSO.registerProducer(p1);
        realTSO.registerProducer(p2);
        IndependentAggregator agg = new IndependentAggregator(realTSO, 15,
                AggregationStrategyImpl.MOVINGHORIZON);
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

        simulator = Simulator.createSimulator(simSteps);
        site1 = new SiteImpl(line1, line2);
        site2 = new SiteImpl(line3, line4);

        p1 = new ConstantOutputGenerator(1600);
        p2 = new RandomOutputGenerator(-300, 70);
        realTSO = new CopperplateTSO(site1, site2);
        realTSO.registerProducer(p1);
        realTSO.registerProducer(p2);
        agg = new IndependentAggregator(realTSO, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(realTSO);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        log.info("Setup 2 done. Starting simulation.");
        simulator.start();
        log.info("Simulation 2 done");
        double profitAfter = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();

        log.info("Profit no curt: {} Profit Curt: {}", profitBefore,
                profitAfter);
        assertTrue(profitBefore < profitAfter);
        return profitAfter - profitBefore;
    }

    @Test
    public void testRepeatForDeterminism() {
        List<Double> results = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            results.add(testAggregationRunStepWithCurtailment(1500));
        }
        log.debug("Result array: {}", Arrays.toString(results.toArray()));
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);
    }

    @Test
    public void testRepeatForDeterminismMultiThread() {
        int expsize = 15;
        final List<Double> results = Lists.newArrayList();
        for (int i = 0; i < expsize; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    results.add(testAggregationRunStepWithCurtailment(1500));
                }
            }).start();
        }
        while (results.size() < expsize) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.debug("Result array: {}", Arrays.toString(results.toArray()));

        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);
    }

    @Test
    public void testRepeatForDeterminismMultiThreadCPTSO() {
        int expsize = 15;
        final List<Double> results = Lists.newArrayList();
        for (int i = 0; i < expsize; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    results.add(testAggregationWithConnectedTSO(1500));
                }
            }).start();
        }
        while (results.size() < expsize) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);
    }

    @Test
    public void testRepeatForDeterminismCPTSO() {
        List<Double> results = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            results.add(testAggregationWithConnectedTSO(1500));

        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);
    }

    @Test
    public void testDifferentSeedsCPTSO() {
        for (int i = 900; i <= 1500; i += 300) {
            testAggregationWithConnectedTSO(i);
        }
    }

    @Test
    public void testDifferentSeedsRegular() {
        for (int i = 900; i <= 1500; i += 300) {
            testAggregationRunStepWithCurtailment(i);
        }
    }

    @Test
    public void testAggregationRunStepNoCurtRunner() {
        testAggregationRunStepNoCurt(1500);
    }

    public double testAggregationRunStepNoCurt(int simSteps) {
        ProductionLine line1 = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(20)
                .addConsuming(3).addCurtailableShifted(3)
                .addCurtailableShifted(3).addConsuming(3).build();
        ProductionLine line2 = new ProductionLineBuilder()
                .setWorkingConsumption(400).setIdleConsumption(60)
                .addConsuming(6).addCurtailableShifted(6)
                .addCurtailableShifted(6).addConsuming(6).build();
        ProductionLine line3 = new ProductionLineBuilder()
                .setWorkingConsumption(600).setIdleConsumption(10)
                .addConsuming(3).addCurtailableShifted(3)
                .addCurtailableShifted(3).addConsuming(3).build();
        ProductionLine line4 = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(15)
                .addConsuming(4).addCurtailableShifted(4)
                .addCurtailableShifted(4).addConsuming(4).build();

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

        Simulator simulator = Simulator.createSimulator(simSteps);
        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);

        EnergyProductionTrackable p2 = new RandomOutputGenerator(0, 1);
        CopperplateTSO realTSO = new CopperplateTSO(site1, site2);
        realTSO.registerProducer(p2);
        IndependentAggregator agg = new IndependentAggregator(realTSO, 15);
        // agg.registerClient(site1);
        // agg.registerClient(site2);

        // simulator.register(agg);
        simulator.register(realTSO);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        log.info("Setup 1 done. Starting simulation");
        simulator.start();

        double profitBefore = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        log.info("Simulation 1 done");

        // After: Curtailment
        line1 = new ProductionLineBuilder().setWorkingConsumption(500)
                .setIdleConsumption(20).addConsuming(3)
                .addCurtailableShifted(3).addCurtailableShifted(3)
                .addConsuming(3).build();
        line2 = new ProductionLineBuilder().setWorkingConsumption(400)
                .setIdleConsumption(60).addConsuming(6)
                .addCurtailableShifted(6).addCurtailableShifted(6)
                .addConsuming(6).build();
        line3 = new ProductionLineBuilder().setWorkingConsumption(600)
                .setIdleConsumption(10).addConsuming(3)
                .addCurtailableShifted(3).addCurtailableShifted(3)
                .addConsuming(3).build();
        line4 = new ProductionLineBuilder().setWorkingConsumption(500)
                .setIdleConsumption(15).addConsuming(4)
                .addCurtailableShifted(4).addCurtailableShifted(4)
                .addConsuming(4).build();

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

        simulator = Simulator.createSimulator(simSteps);
        site1 = new SiteImpl(line1, line2);
        site2 = new SiteImpl(line3, line4);

        EnergyProductionTrackable p1 = new ConstantOutputGenerator(1600);
        p2 = new RandomOutputGenerator(-300, 70);
        realTSO = new CopperplateTSO(site1, site2);
        realTSO.registerProducer(p1);
        realTSO.registerProducer(p2);

        agg = new IndependentAggregator(realTSO, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(realTSO);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        log.info("Setup 2 done. Starting simulation");
        simulator.start();
        log.info("Simulation 2 done");
        double profitAfter = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();
        log.info("Profit no curt: {} Profit Curt: {}", profitBefore,
                profitAfter);
        assertEquals(profitBefore, profitAfter, 0);
        return profitAfter - profitBefore;
    }

    @Test
    public void testAggregationWithConnectedTSORunnerAndRFSteerable() {
        testAggregationWithConnectedTSO(1500);
    }

    public double testAggregationWithConnectedTSOAndRFSteerable(int simSteps) {
        ProductionLine line1 = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(20)
                .setRfHighConsumption(800).setRfLowConsumption(400)
                .addConsuming(3).addCurtailableShifted(6)
                .addCurtailableShifted(4).addConsuming(3)
                .addRFSteerableStation(1, 10).build();
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

        line1.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3, 30));
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

        Simulator simulator = Simulator.createSimulator(simSteps);
        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);

        EnergyProductionTrackable p2 = new RandomOutputGenerator(0, 1);
        CopperplateTSO realTSO = new CopperplateTSO(site1, site2);
        realTSO.registerProducer(p2);
        IndependentAggregator agg = new IndependentAggregator(realTSO, 15);
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
                .setIdleConsumption(20).setRfHighConsumption(800)
                .setRfLowConsumption(400).addConsuming(3)
                .addCurtailableShifted(6).addCurtailableShifted(4)
                .addConsuming(3).addRFSteerableStation(1, 10).build();
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

        line1.deliverResources(ResourceFactory.createBulkMPResource(3000, 3, 3,
                3, 3, 30));
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

        simulator = Simulator.createSimulator(simSteps);
        site1 = new SiteImpl(line1, line2);
        site2 = new SiteImpl(line3, line4);

        EnergyProductionTrackable p1 = new ConstantOutputGenerator(1600);
        p2 = new RandomOutputGenerator(-300, 70);
        realTSO = new CopperplateTSO(site1, site2);
        realTSO.registerProducer(p1);
        realTSO.registerProducer(p2);
        agg = new IndependentAggregator(realTSO, 15);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
        simulator.register(realTSO);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        log.info("Setup 2 done. Starting simulation.");
        simulator.start();
        log.info("Simulation 2 done");
        double profitAfter = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();

        log.info("Profit no curt: {} Profit Curt: {}", profitBefore,
                profitAfter);
        assertTrue(profitBefore < profitAfter);
        return profitAfter - profitBefore;
    }

    @Test
    public void testAggregationWithBalancingTSO() {
        testAggregationWithBalancingTSO(1000);
    }

    public double testAggregationWithBalancingTSO(int simSteps) {
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

        Simulator simulator = Simulator.createSimulator(simSteps);
        Site site1 = new SiteImpl(line1, line2);
        Site site2 = new SiteImpl(line3, line4);

        EnergyProductionTrackable p1 = new ConstantOutputGenerator(1600);
        EnergyProductionTrackable p2 = new RandomOutputGenerator(-300, 70);
        CopperplateTSO realTSO = new CopperplateTSO(site1, site2);
        realTSO.registerProducer(p1);
        realTSO.registerProducer(p2);
        IndependentAggregator agg = new IndependentAggregator(realTSO, 1,
                AggregationStrategyImpl.MOVINGHORIZON);
        agg.registerClient(site1);
        agg.registerClient(site2);

        simulator.register(agg);
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

        // After: CopperplateTSO
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

        simulator = Simulator.createSimulator(simSteps);
        site1 = new SiteImpl(line1, line2);
        site2 = new SiteImpl(line3, line4);

        p1 = new ConstantOutputGenerator(1600);
        p2 = new RandomOutputGenerator(-300, 70);
        BalancingTSO realTSO2 = new BalancingTSO(site1, site2);
        realTSO2.registerProducer(p1);
        realTSO2.registerProducer(p2);
        ReactiveMechanismAggregator agg2 = new ReactiveMechanismAggregator(
                realTSO2, AggregationStrategyImpl.MOVINGHORIZON);
        agg2.registerClient(site1);
        agg2.registerClient(site2);

        simulator.register(agg2);
        simulator.register(realTSO2);
        simulator.register(t1);
        simulator.register(t2);
        simulator.register(t3);
        simulator.register(t4);
        log.info("Setup 2 done. Starting simulation.");
        simulator.start();
        log.info("Simulation 2 done");
        double profitAfter = t1.getTotalProfit() + t2.getTotalProfit()
                + t3.getTotalProfit() + t4.getTotalProfit();

        log.info("Profit BalancingTSO: {} Profit OldTSO: {}", profitBefore,
                profitAfter);
        assertEquals(profitBefore, profitAfter, 0);
        return profitAfter - profitBefore;
    }

    @Test
    public void gameScenarioTestExample1() {
        GameConfiguratorEx ex = new GameConfiguratorEx(0);
        Game<Site, Aggregator> g = new Game<>(3, ex, 20);
        g.runExperiment();
        new GameResultWriter<>(g, "CONSOLE").write();
        assertTrue(g.getFormattedResultString().contains(
                "C:PayoffEntry [0, 3]->20"));
        assertTrue(g.getFormattedResultString().contains(
                "V:PayoffEntry [0, 3]->[729632.5, 763297.5, 678570.0]"));

    }
}
