package domain.factory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import simulation.SimulationComponent;
import simulation.SimulationContext;
import simulation.Simulator;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.eventbus.Subscribe;

import domain.resource.Resource;
import domain.resource.ResourceFactory;

public class ProductionLineTest {

    public static class ChangeEventComponent implements SimulationComponent {
        private Map<String, Object> resultMap = new HashMap<>();
        private String lastType = "";
        private SimulationComponent mock;

        public ChangeEventComponent(SimulationComponent mock) {
            this.mock = mock;
        }

        @Override
        public void afterTick() {
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
                mock.tick();
            }
        }

        @Override
        public void tick() {
        }

    }

    // Mocks for avoiding null checks.
    private ProductionLine lineSimple;
    private ProductionLine lineExtended;
    private int simSteps;

    @SuppressWarnings("null")
    private SimulationContext sim = mock(SimulationContext.class);

    public ProductionLineTest() {
        lineSimple = ProductionLine.createSimpleLayout();
        lineExtended = ProductionLine.createExtendedLayout();
    }

    @Before
    public void setUp() throws Exception {
        lineSimple = ProductionLine.createSimpleLayout();
        lineExtended = ProductionLine.createExtendedLayout();
        simSteps = 20;
        sim = Simulator.createSimulator(simSteps);
        sim.register(lineSimple);
        sim.register(lineExtended);
    }

    @Test
    public void signalConsumptionTest() {
        int n = 3;
        deliverResources(n);
        SimulationComponent m = mock(SimulationComponent.class);
        SimulationComponent tester = new ChangeEventComponent(m);
        long duration = 20;
        sim = Simulator.createSimulator(duration);
        sim.register(lineSimple);
        sim.register(tester);
        ((Simulator) sim).start();
        verify(m, times((int) duration)).tick();
        assertEquals("simulation:stopped", ((ChangeEventComponent) tester).getLastType());
    }

    @Test
    public void testDeliverAndProcessResources() {
        int n = 3;
        deliverResources(n);
        SimulationComponent tester = mock(SimulationComponent.class);
        sim.register(tester);
        ((Simulator) sim).start();
        verify(tester, times(simSteps)).tick();
        assertEquals(n, lineExtended.takeResources().size());

    }

    @Test
    public void testInitialExtendedSetup() {
        assertEquals(4, lineExtended.getNumberOfWorkstations());
        assertEquals(0, lineExtended.takeResources().size());
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

}
