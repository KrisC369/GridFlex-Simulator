package time;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
public class ClockTest {
    Clock c = mock(Clock.class);

    @Before
    public void setUp() throws Exception {
        c = new Clock();
    }

    @Test
    public void testInitialDefault() {
        assertEquals(0, c.getTimeCount());
    }

    @Test
    public void testAdvanceTime() {
        c.addTimeStep(10);
        assertEquals(10, c.getTimeCount());
    }

    @Test
    public void resetTime() {
        testAdvanceTime();
        c.resetTime();
        assertEquals(0, c.getTimeCount());
    }

}
