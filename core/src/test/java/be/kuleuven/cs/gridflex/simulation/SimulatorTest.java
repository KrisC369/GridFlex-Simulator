package be.kuleuven.cs.gridflex.simulation;

import be.kuleuven.cs.gridflex.event.Event;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SimulatorTest {
    public static class ChangeEventComponent implements SimulationComponent {
        private Map<String, Object> resultMap = new LinkedHashMap<>();

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
    }

    private SimulationComponent comp = mock(SimulationComponent.class);
    private final int defaultRunTime = 1;
    private Simulator s = Simulator.createSimulator(defaultRunTime);

    @Before
    public void setUp() throws Exception {
        s = Simulator.createSimulator(defaultRunTime);
        comp = mock(SimulationComponent.class);
    }

    @Test
    public void testEventBus() {
        int duration = 20;
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
        assertEquals(0, s.getSimulationClock().getTimeCount());
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
        int duration = 20;
        s = Simulator.createSimulator(duration);
        runSim(true);
        verify(comp, times(20)).tick(anyInt());
    }

    @Test
    public void testRunDurationImmediateReturnAfterTick() {
        int duration = 20;
        s = Simulator.createSimulator(duration);
        runSim(true);
        verify(comp, times(20)).afterTick(anyInt());
    }

    @Test
    public void testSimDuration() {
        int duration = 20;
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
        InstrumentationComponent i = mock(InstrumentationComponent.class);
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

    @Test
    public void testUID() {
        s = Simulator.createSimulator(2000);
        UIDGenerator gen = s.getUIDGenerator();
        long prev = -1;
        for (long c = 0; c < 100000; c++) {
            long curr = gen.getNextUID();
            assertNotEquals(prev, curr);
            prev = curr;
        }
    }

    @Test
    public void testGenerator() {
        List<List<Long>> results = Lists.newArrayList();
        List<Long> tmp = Lists.newArrayList();

        for (int i = 0; i < 100; i++) {
            tmp = Lists.newArrayList();
            s = Simulator.createSimulator(2000);
            for (int j = 0; j < 4000; j++) {
                tmp.add(s.getRandom().nextLong());
            }
            results.add(tmp);
        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);

        results = Lists.newArrayList();
        tmp = Lists.newArrayList();

        for (int i = 0; i < 100; i++) {
            tmp = Lists.newArrayList();
            s = Simulator.createSimulator(31648);
            for (int j = 0; j < 4000; j++) {
                tmp.add(s.getRandom().nextLong());
            }
            results.add(tmp);
        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);

        results = Lists.newArrayList();
        tmp = Lists.newArrayList();

        for (int i = 0; i < 100; i++) {
            tmp = Lists.newArrayList();
            s = Simulator.createSimulator(12447);
            for (int j = 0; j < 4000; j++) {
                tmp.add(s.getRandom().nextLong());
            }
            results.add(tmp);
        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);

        results = Lists.newArrayList();
        tmp = Lists.newArrayList();

        for (int i = 0; i < 100; i++) {
            tmp = Lists.newArrayList();
            s = Simulator.createSimulator(358);
            for (int j = 0; j < 4000; j++) {
                tmp.add(s.getRandom().nextLong());
            }
            results.add(tmp);
        }
        assertEquals(1, Sets.newLinkedHashSet(results).size(), 0);
    }

    @Test
    public void testGeneratorDeterminism() {
        Simulator sim = Simulator.createSimulator(24550);
        List<Integer> res = Lists.newArrayList();
        List<Integer> t = Lists.newArrayList();

        for (int i = 0; i < 10000; i++) {
            sim = Simulator.createSimulator(24550);
            t = Lists.newArrayList();
            for (int j = 0; j < 10000; j++) {
                t.add(sim.getRandom().nextInt(370) - 300);
            }
            res.add(t.hashCode());
        }
        // System.out.println(Arrays.toString(res.toArray()));
        assertEquals(1, Sets.newLinkedHashSet(res).size(), 0);
    }

    @Test
    public void testMersenneTwister() {
        MersenneTwister u1 = new MersenneTwister(70);
        MersenneTwister u2 = new MersenneTwister(70);
        List<Integer> res1 = Lists.newArrayList();
        List<Integer> res2 = Lists.newArrayList();
        for (int i = 0; i < 100000; i++) {
            res1.add((int) (u1.nextDouble() * 100));
            res2.add((int) (u2.nextDouble() * 100));
        }
        boolean ass = true;
        for (int i = 0; i < res1.size(); i++) {
            if (!res1.get(i).equals(res2.get(i))) {
                ass = false;
            }
        }
        assertTrue(ass);
    }
}
