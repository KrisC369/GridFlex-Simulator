package time;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

public class ClockTest {
    Clock c = mock(Clock.class);

    @Test
    public void resetTime() {
        testAdvanceTime();
        c.resetTime();
        assertEquals(0, c.getTimeCount());
    }

    @Before
    public void setUp() throws Exception {
        c = new Clock();
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
