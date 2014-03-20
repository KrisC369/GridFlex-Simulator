package simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.Subscribe;

import events.SimStateEvent;

public class SimulatorTest {
    private Simulator s = mock(Simulator.class);
    private ISimulationComponent comp = mock(ISimulationComponent.class);
    private final long defaultRunTime = 1;

    @Before
    public void setUp() throws Exception {
        s = Simulator.createSimulator(defaultRunTime);
        comp = mock(ISimulationComponent.class);
    }

    @Test
    public void testInitialState() {
        assertEquals(0, s.getSimulationTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDurationInit() {
        s = Simulator.createSimulator(0);  
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroDurationInit() {
        s = Simulator.createSimulator(0);  
    }

    @Test
    public void testStartedSim() {
        s.start();
        assertEquals(defaultRunTime, s.getSimulationTime());
        assertEquals(s.getDuration(), s.getSimulationTime());
    }

    @Test
    public void testSimDuration() {
        long duration = 20;
        s = Simulator.createSimulator(duration);
        assertEquals(duration, s.getDuration());
    }

    @Test
    public void testRegisterComp() {
        s.register(comp);
        assertEquals(1, s.getComponents().size());
        verify(comp,times(1)).initialize(s);
    }

    @Test
    public void testRunDurationImmediateReturn() {
        long duration = 20;
        s = Simulator.createSimulator(duration);
        runSim(true);
        verify(comp, times(20)).tick();
    }
    
    @Test
    public void testEventBus(){
        long duration = 20;
        s = Simulator.createSimulator(duration);
        comp = new ChangeEventComponent();
        s.register(comp);
        s.start();
        assertNotNull(((ChangeEventComponent) comp).getResult());
        assertFalse(((ChangeEventComponent) comp).getResult().isEmpty());
    }

    private void runSim(boolean immediateReturn) {
        s.register(comp);
        s.start();
    }

    public static class ChangeEventComponent implements ISimulationComponent{
        private Map<String, Object> resultMap = new HashMap<>();
        @Override
        public void initialize(ISimulationContext context) {
        }

        @Override
        public void tick() {
        }
        
        @Subscribe public void recordCustomerChange(SimStateEvent e) {
            resultMap = (e.getEventInfo());
        }
        public Map<String, Object> getResult(){
            return resultMap;
        }
    }
}
