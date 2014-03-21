package simulation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.eventbus.Subscribe;

public class SimulatorTest {
    public static class ChangeEventComponent implements SimulationComponent {
        private Map<String, Object> resultMap = new HashMap<>();

        @Override
        public void afterTick() {
            // TODO Auto-generated method stub

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
        }

        @Override
        public void tick() {
        }
    }

    private Simulator s = mock(Simulator.class);
    private SimulationComponent comp = mock(SimulationComponent.class);

    private final long defaultRunTime = 1;

    @Before
    public void setUp() throws Exception {
        s = Simulator.createSimulator(defaultRunTime);
        comp = mock(SimulationComponent.class);
    }

    @Test
    public void testEventBus() {
        long duration = 20;
        s = Simulator.createSimulator(duration);
        comp = new ChangeEventComponent();
        s.register(comp);
        s.start();
        assertNotNull(((ChangeEventComponent) comp).getResult());
        assertFalse(((ChangeEventComponent) comp).getResult().isEmpty());
    }

    @Test
    public void testInitialState() {
        assertEquals(0, s.getSimulationTime());
        assertEquals(0,s.getSimulationClock().getTimeCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDurationInit() {
        s = Simulator.createSimulator(0);
    }

    @Test
    public void testRegisterComp() {
        s.register(comp);
        assertEquals(1, s.getSimulationComponents().size());
        verify(comp, times(1)).initialize(s);
    }

    @Test
    public void testRunDurationImmediateReturn() {
        long duration = 20;
        s = Simulator.createSimulator(duration);
        runSim(true);
        verify(comp, times(20)).tick();
    }

    @Test
    public void testRunDurationImmediateReturnAfterTick() {
        long duration = 20;
        s = Simulator.createSimulator(duration);
        runSim(true);
        verify(comp, times(20)).afterTick();
    }

    @Test
    public void testSimDuration() {
        long duration = 20;
        s = Simulator.createSimulator(duration);
        assertEquals(duration, s.getDuration());
    }

    @Test
    public void testStartedSim() {
        s.start();
        assertEquals(defaultRunTime, s.getSimulationTime());
        assertEquals(s.getDuration(), s.getSimulationTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroDurationInit() {
        s = Simulator.createSimulator(0);
    }
    
    @Test
    public void testRegisterInstrumentation() {
        s = Simulator.createSimulator(20);
        InstrumentationComponent i =mock(InstrumentationComponent.class);
        s.register(i);
        assertTrue(s.getInstrumentationComponents().contains(i));
        assertFalse(s.getSimulationComponents().contains(i));
        s.register(comp);
        assertTrue(s.getInstrumentationComponents().contains(comp));
        assertTrue(s.getSimulationComponents().contains(comp));
    }

    private void runSim(boolean immediateReturn) {
        s.register(comp);
        s.start();
    }
}
