package be.kuleuven.cs.flexsim.time;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.time.SimulationClock;

public class ClockTest {
    SimulationClock c = mock(SimulationClock.class);

    @Test
    public void resetTime() {
        testAdvanceTime();
        c.resetTime();
        assertEquals(0, c.getTimeCount());
    }

    @Before
    public void setUp() throws Exception {
        c = new SimulationClock();
    }

    @Test
    public void testAdvanceTime() {
        c.addTimeStep(10);
        assertEquals(10, c.getTimeCount());
    }

    @Test
    public void testInitialDefault() {
        assertEquals(0, c.getTimeCount());
    }

}
