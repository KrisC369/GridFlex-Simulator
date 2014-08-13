package be.kuleuven.cs.flexsim.domain.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.process.ProductionLine.ProductionLineBuilder;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.CurtailableWorkstation;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.flexsim.simulation.Simulator;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.eventbus.Subscribe;

public class ProductionLineTest {

    public static class ChangeEventComponent implements SimulationComponent {
        private Map<String, Object> resultMap = new LinkedHashMap<>();
        private String lastType = "";
        private SimulationComponent mock;

        public ChangeEventComponent(SimulationComponent mock) {
            this.mock = mock;
        }

        @Override
        public void afterTick(int t) {
        }

        public String getLastType() {
            return lastType;
        }

        public Map<String, Object> getResult() {
            return resultMap;
        }

        @Override
        public void initialize(SimulationContext context) {
        }

        @Subscribe
        public void recordCustomerChange(Event e) {
            resultMap = (e.getAttributes());
            lastType = e.getType();
            if (e.getType().contains("report")) {
                mock.tick(0);
            }
        }

        @Override
        public void tick(int t) {
        }

        @Override
        public List<SimulationComponent> getSimulationSubComponents() {
            // TODO Auto-generated method stub
            return Collections.emptyList();
        }

    }

    // Mocks for avoiding null checks.
    private ProductionLine lineSimple;
    private ProductionLine lineExtended;
    private ProductionLine lineSuperExtended;
    private int simSteps;
    private static final double DELTA = 0.05;

    @SuppressWarnings("null")
    private SimulationContext sim = mock(SimulationContext.class);

    public ProductionLineTest() {
        lineSimple = new ProductionLineBuilder().addShifted(1).build();
        lineExtended = new ProductionLineBuilder().addShifted(3).addShifted(1)
                .build();
        lineSuperExtended = new ProductionLineBuilder().addShifted(3)
                .addShifted(2).addShifted(1).build();
    }

    @Before
    public void setUp() throws Exception {
        lineSimple = new ProductionLineBuilder().addShifted(1).build();
        lineExtended = new ProductionLineBuilder().addShifted(3).addShifted(1)
                .build();
        simSteps = 20;
        sim = Simulator.createSimulator(simSteps);
        sim.register(lineSimple);
        sim.register(lineExtended);
    }

    @Test
    public void testDeliverAndProcessResources() {
        int n = 3;
        deliverResources(n);
        SimulationComponent tester = mock(SimulationComponent.class);
        sim.register(tester);
        startSim();
        verify(tester, times(simSteps)).tick(anyInt());
        assertEquals(n, lineExtended.takeResources().size());

    }

    @Test
    public void testInitialExtendedSetup() {
        assertEquals(4, lineExtended.getNumberOfWorkstations());
        assertEquals(0, lineExtended.takeResources().size());
    }

    @Test
    public void testInitialSuperExtendedSetup() {
        assertEquals(6, lineSuperExtended.getNumberOfWorkstations());
        assertEquals(0, lineSuperExtended.takeResources().size());
    }

    @Test
    public void testInitialSimpleSetup() {
        assertEquals(1, lineSimple.getNumberOfWorkstations());
        assertEquals(0, lineSimple.takeResources().size());
    }

    private void deliverResources(int n) {
        List<Resource> res = ResourceFactory.createBulkMPResource(n, 3, 1);
        lineExtended.deliverResources(res);
    }

    @Test
    public void testCustomSetup() {
        ProductionLine lineSimple = ProductionLine.createCustomLayout(1, 3, 1);
        ProductionLine lineExtended = ProductionLine.createCustomLayout(4, 3,
                1, 2);
        assertEquals(5, lineSimple.getNumberOfWorkstations());
        assertEquals(10, lineExtended.getNumberOfWorkstations());
    }

    @Test
    public void testGetCurtailables() {
        ProductionLine lineExtended = new ProductionLineBuilder().addShifted(7)
                .addCurtailableShifted(7).addShifted(3).build();
        List<CurtailableWorkstation> stations = lineExtended
                .getCurtailableStations();
        for (CurtailableWorkstation c : stations) {
            assertTrue(lineExtended.getWorkstations().contains(c));
        }
    }

    @Test
    public void testBuilderDefault() {
        ProductionLine l = new ProductionLineBuilder().addDefault(3)
                .addDefault(4).build();
        setupForSim(l, simSteps);
        assertEquals(7, l.getNumberOfWorkstations());
        startSim();
        assertEquals(0, l.getWorkstations().get(3).getTotalConsumption(), DELTA);
    }

    @Test
    public void testBuilderSteerable() {
        ProductionLine l = new ProductionLineBuilder().addDefault(3)
                .addDefault(4).addMultiCapExponentialConsuming(1, 12).build();
        setupForSim(l, simSteps);
        assertEquals(8, l.getNumberOfWorkstations());
        startSim();
        assertEquals(0, l.getWorkstations().get(3).getTotalConsumption(), DELTA);
    }

    @Test
    public void testBuilderConsuming() {
        ProductionLine l = new ProductionLineBuilder().addConsuming(3)
                .addConsuming(4).build();
        setupForSim(l, simSteps);
        assertEquals(7, l.getNumberOfWorkstations());
        startSim();
        assertNotEquals(0, l.getWorkstations().get(3).getTotalConsumption());
    }

    @Test
    public void testBuilderMultiCap() {
        ProductionLine l = new ProductionLineBuilder().addConsuming(3)
                .addConsuming(4).addMultiCapConstantConsuming(1, 12)
                .addMultiCapExponentialConsuming(1, 12)
                .addMultiCapLinearConsuming(1, 12).build();
        setupForSim(l, simSteps);
        assertEquals(10, l.getNumberOfWorkstations());
        startSim();
        assertNotEquals(0, l.getWorkstations().get(3).getTotalConsumption());
    }

    @Test
    public void testBuilderRFS() {
        ProductionLine l = new ProductionLineBuilder().addConsuming(3)
                .addConsuming(4).addMultiCapConstantConsuming(1, 12)
                .addMultiCapExponentialConsuming(1, 12)
                .addMultiCapLinearConsuming(1, 12).addRFSteerableStation(3, 12)
                .build();
        setupForSim(l, simSteps);
        assertEquals(13, l.getNumberOfWorkstations());
        startSim();
        assertNotEquals(0, l.getWorkstations().get(3).getTotalConsumption());
        assertNotEquals(0, l.getDualModeStations().size());
        assertNotEquals(0, l.getSteerableStations().size());
    }

    @Test
    public void testBuilderSettings() {
        ProductionLine l = new ProductionLineBuilder().setIdleConsumption(0)
                .setMulticapWorkingConsumption(0).setRfHighConsumption(0)
                .setRfLowConsumption(0).setRfWidth(1).setWorkingConsumption(0)
                .addConsuming(3).addConsuming(4)
                .addMultiCapConstantConsuming(1, 12)
                .addMultiCapExponentialConsuming(1, 12)
                .addMultiCapLinearConsuming(1, 12).build();
        setupForSim(l, simSteps);
        assertEquals(10, l.getNumberOfWorkstations());
        startSim();
        assertEquals(0, l.getWorkstations().get(3).getTotalConsumption(), 0);
        assertEquals(0, l.getDualModeStations().size(), 0);
    }

    /**
     * 
     */
    private void startSim() {
        ((Simulator) sim).start();
    }

    private void setupForSim(ProductionLine l, int steps) {
        sim = Simulator.createSimulator(steps);
        sim.register(l);
        List<Resource> res = ResourceFactory.createBulkMPResource(50, 3, 3, 3,
                3, 3, 3);
        l.deliverResources(res);
    }

    @Test
    public void testFlexNoSteer() {
        ProductionLine l = new ProductionLineBuilder().addConsuming(3)
                .addShifted(4).build();
        setupForSim(l, simSteps);
        startSim();
        List<FlexTuple> flex = l.getCurrentFlexbility();
        assertEquals(1, flex.size(), 0);
        assertTrue(flex.contains(FlexTuple.NONE));
    }

    @Test
    public void testFlexNoCurt() {
        ProductionLine l = new ProductionLineBuilder().addConsuming(3)
                .addShifted(4).build();
        setupForSim(l, simSteps);
        startSim();
        List<FlexTuple> flex = l.getCurrentFlexbility();
        assertEquals(1, flex.size(), 0);
        assertTrue(flex.contains(FlexTuple.NONE));

        // with steer
        l = new ProductionLineBuilder().addConsuming(3)
                .addRFSteerableStation(4, 20).build();
        setupForSim(l, simSteps);
        startSim();
        flex = l.getCurrentFlexbility();
        assertEquals(1, flex.size(), 0);
        assertTrue(flex.contains(FlexTuple.NONE));
    }

    @Test
    public void testFlex1CurtAlreadyCurt() {
        ProductionLine l = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(10)
                .addConsuming(3).addCurtailableShifted(4).addConsuming(3)
                .build();
        setupForSim(l, simSteps);
        l.getCurtailableStations().get(0).doFullCurtailment();
        startSim();
        List<FlexTuple> flex = l.getCurrentFlexbility();
        assertEquals(1, flex.size(), 0);
        System.out.println(flex);
        assertFalse(flex.contains(FlexTuple.NONE));
        assertTrue(flex.get(0).getDirection());// one up flex
    }

    @Test
    public void testFlex1Curt() {
        ProductionLine l = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(10)
                .addConsuming(3).addCurtailableShifted(4).addConsuming(3)
                .build();
        setupForSim(l, simSteps);
        startSim();
        List<FlexTuple> flex = l.getCurrentFlexbility();
        assertEquals(1, flex.size(), 0);
        assertFalse(flex.contains(FlexTuple.NONE));
        assertEquals(10, flex.get(0).getDeltaP(), 10); // it's idle anyways so
        // only fixd consumption
        // counted.
    }

    @Test
    public void testFlex2Curt() {
        ProductionLine l = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(10)
                .addConsuming(3).addCurtailableShifted(5).addConsuming(3)
                .build();
        setupForSim(l, simSteps);
        startSim();
        List<FlexTuple> flex = l.getCurrentFlexbility();
        assertEquals(3, flex.size(), 0); // l1: 2, l2: 1
        assertFalse(flex.contains(FlexTuple.NONE));
        assertEquals(20, flex.get(0).getDeltaP(), 10);

    }

    @Test
    public void testFlex3Curt() {
        ProductionLine l = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(10)
                .addConsuming(3).addCurtailableShifted(6).addConsuming(3)
                .build();
        setupForSim(l, simSteps);
        startSim();
        List<FlexTuple> flex = l.getCurrentFlexbility();
        assertEquals(6, flex.size(), 0); // 3 r1, 6 r2, 1 r3
        assertFalse(flex.contains(FlexTuple.NONE));
        assertEquals(30, flex.get(0).getDeltaP(), 20);

    }

    @Test
    public void testFlex2CurtHorizontal() {
        ProductionLine l = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(10)
                .addConsuming(3).addCurtailableShifted(4)
                .addCurtailableShifted(4).addConsuming(3).build();
        setupForSim(l, simSteps);
        startSim();
        List<FlexTuple> flex = l.getCurrentFlexbility();
        assertEquals(3, flex.size(), 0); // 2 r1, 0 r2, 1 r3
        assertFalse(flex.contains(FlexTuple.NONE));
        assertEquals(30, flex.get(0).getDeltaP(), 20);
        assertFalse(hasPositiveFlex(flex));
    }

    @Test
    public void testUpFlexCurt() {
        ProductionLine l = new ProductionLineBuilder()
                .setWorkingConsumption(500).setIdleConsumption(10)
                .addConsuming(3).addCurtailableShifted(4)
                .addCurtailableShifted(4).addConsuming(3).build();
        setupForSim(l, simSteps);
        startSim();
        List<FlexTuple> flex = l.getCurrentFlexbility();
        assertEquals(3, flex.size(), 0); // 2 r1, 0 r2, 1 r3
        assertFalse(flex.contains(FlexTuple.NONE));
        assertEquals(30, flex.get(0).getDeltaP(), 20);
        l.executeCurtailmentProfile(flex.get(2).getId()); // all stations curt.
        l.tick(simSteps + 1);
        flex = l.getCurrentFlexbility();
        boolean hasPositiveFlex = false;
        for (FlexTuple f : flex) {
            if (f.getDirection())
                hasPositiveFlex = true;
        }
        assertTrue(flex.size() > 0);
        assertEquals(true, hasPositiveFlex);
    }

    private boolean hasPositiveFlex(List<FlexTuple> flex) {
        for (FlexTuple f : flex) {
            if (f.getDirection())
                return true;
        }
        return false;
    }

    @Test
    public void testLayout() {
        ProductionLine l = new ProductionLineBuilder().addConsuming(3)
                .addShifted(4).addMultiCapConstantConsuming(3, 12).build();
        assertTrue(l.toString().contains(l.getLayout().toString()));
    }
}
