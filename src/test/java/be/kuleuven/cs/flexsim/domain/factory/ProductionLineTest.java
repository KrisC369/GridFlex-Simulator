package be.kuleuven.cs.flexsim.domain.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.factory.ProductionLine.ProductionLineBuilder;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.resource.ResourceFactory;
import be.kuleuven.cs.flexsim.domain.workstation.Curtailable;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import be.kuleuven.cs.flexsim.simulation.TimeStepSimulator;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.eventbus.Subscribe;

public class ProductionLineTest {

    public static class ChangeEventComponent implements SimulationComponent {
        private Map<String, Object> resultMap = new HashMap<>();
        private String lastType = "";
        private SimulationComponent mock;

        public ChangeEventComponent(SimulationComponent mock) {
            this.mock = mock;
        }

        @Override
        public void afterTick(int t) {
            // TODO Auto-generated method stub

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

    private SimulationContext context;
    @SuppressWarnings("null")
    private TimeStepSimulator sim = TimeStepSimulator.createSimulator(1,
            context);

    public ProductionLineTest() {
        lineSimple = ProductionLine.createSimpleLayout();
        lineExtended = ProductionLine.createExtendedLayout();
        lineSuperExtended = ProductionLine.createSuperExtendedLayout();
        this.context = SimulationContext.createDefaultContext();
    }

    @Before
    public void setUp() throws Exception {
        lineSimple = ProductionLine.createSimpleLayout();
        lineExtended = ProductionLine.createExtendedLayout();
        simSteps = 20;
        sim = TimeStepSimulator.createSimulator(simSteps, context);
        context.register(lineSimple);
        context.register(lineExtended);
    }

    @Test
    public void testDeliverAndProcessResources() {
        int n = 3;
        deliverResources(n);
        SimulationComponent tester = mock(SimulationComponent.class);
        context.register(tester);
        ((TimeStepSimulator) sim).start();
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
        ProductionLine lineExtended = ProductionLine
                .createStaticCurtailableLayout();
        List<Curtailable> stations = lineExtended.getCurtailableStations();
        for (Curtailable c : stations) {
            assertTrue(lineExtended.getWorkstations().contains(c));
        }
    }

    @Test
    public void testBuilderDefault() {
        ProductionLine l = new ProductionLineBuilder().addDefault(3)
                .addDefault(4).build();
        sim = TimeStepSimulator.createSimulator(200, context);
        context.register(l);
        List<Resource> res = ResourceFactory.createBulkMPResource(50, 3, 1);
        l.deliverResources(res);
        assertEquals(7, l.getNumberOfWorkstations());
        ((TimeStepSimulator) sim).start();
        assertEquals(0, l.getWorkstations().get(3).getTotalConsumption(), DELTA);
    }

    @Test
    public void testBuilderConsuming() {
        ProductionLine l = new ProductionLineBuilder().addConsuming(3)
                .addConsuming(4).build();
        sim = TimeStepSimulator.createSimulator(200, context);
        context.register(l);
        List<Resource> res = ResourceFactory.createBulkMPResource(50, 3, 1);
        l.deliverResources(res);
        assertEquals(7, l.getNumberOfWorkstations());
        ((TimeStepSimulator) sim).start();
        assertNotEquals(0, l.getWorkstations().get(3).getTotalConsumption());
    }

    @Test
    public void testBuilderMultiCap() {
        ProductionLine l = new ProductionLineBuilder().addConsuming(3)
                .addConsuming(4).addMultiCapConstantConsuming(1, 12)
                .addMultiCapExponentialConsuming(1, 12)
                .addMultiCapLinearConsuming(1, 12).build();
        sim = TimeStepSimulator.createSimulator(200, context);
        context.register(l);
        List<Resource> res = ResourceFactory.createBulkMPResource(50, 3, 1);
        l.deliverResources(res);
        assertEquals(10, l.getNumberOfWorkstations());
        ((TimeStepSimulator) sim).start();
        assertNotEquals(0, l.getWorkstations().get(3).getTotalConsumption());
    }

}
