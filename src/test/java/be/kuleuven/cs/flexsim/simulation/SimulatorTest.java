package be.kuleuven.cs.flexsim.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.eventbus.Subscribe;

public class SimulatorTest {
    public static class ChangeEventComponent implements SimulationComponent {
        private Map<String, Object> resultMap = new HashMap<>();

        @Override
        public void afterTick(int t) {
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
        public void tick(int t) {
        }

        @Override
        public List<SimulationComponent> getSimulationSubComponents() {
            // TODO Auto-generated method stub
            return Collections.emptyList();
        }
    }

    private SimulationComponent comp = mock(SimulationComponent.class);
    private final long defaultRunTime = 1;
    private SimulationContext context = mock(SimulationContext.class);
    private TimeStepSimulator s = TimeStepSimulator.createSimulator(
            defaultRunTime, context);

    @Before
    public void setUp() throws Exception {
        context = SimulationContext.createDefaultContext();
        s = TimeStepSimulator.createSimulator(defaultRunTime, context);
        comp = mock(SimulationComponent.class);
    }

    @Test
    public void testEventBus() {
        long duration = 20;
        s = TimeStepSimulator.createSimulator(duration, context);
        comp = new ChangeEventComponent();
        context.register(comp);
        s.start();
        assertNotNull(((ChangeEventComponent) comp).getResult());
        assertFalse(((ChangeEventComponent) comp).getResult().isEmpty());
    }

    @Test
    public void testInitialState() {
        assertEquals(0, s.getSimulationTime());
        assertEquals(0, s.getClock().getTimeCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDurationInit() {
        s = TimeStepSimulator.createSimulator(0, context);
    }

    @Test
    public void testRegisterComp() {
        context.register(comp);
        assertEquals(1, context.getSimulationComponents().size());
        verify(comp, times(1)).initialize(context);
    }

    @Test
    public void testRunDurationImmediateReturn() {
        long duration = 20;
        s = TimeStepSimulator.createSimulator(duration, context);
        runSim(true);
        verify(comp, times(20)).tick(anyInt());
    }

    @Test
    public void testRunDurationImmediateReturnAfterTick() {
        long duration = 20;
        s = TimeStepSimulator.createSimulator(duration, context);
        runSim(true);
        verify(comp, times(20)).afterTick(anyInt());
    }

    @Test
    public void testSimDuration() {
        long duration = 20;
        s = TimeStepSimulator.createSimulator(duration, context);
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
        s = TimeStepSimulator.createSimulator(0, context);
    }

    @Test
    public void testRegisterInstrumentation() {
        s = TimeStepSimulator.createSimulator(20, context);
        InstrumentationComponent i = mock(InstrumentationComponent.class);
        context.register(i);
        assertTrue(context.getInstrumentationComponents().contains(i));
        assertFalse(context.getSimulationComponents().contains(i));
        context.register(comp);
        assertTrue(context.getInstrumentationComponents().contains(comp));
        assertTrue(context.getSimulationComponents().contains(comp));
    }

    private void runSim(boolean immediateReturn) {
        context.register(comp);
        s.start();
    }
}
