package be.kuleuven.cs.flexsim.domain.tso;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Before;
import org.junit.Test;

public class RandomTSOTest {
    private SteeringSignal tso = mock(SteeringSignal.class);

    private final int MIN = -44;
    private final int MAX = 70;

    @Before
    public void setUp() throws Exception {
        tso = new RandomTSO(MIN, MAX, new MersenneTwister());
    }

    @Test
    public void testSequence() {
        for (int i = 0; i < 30000; i++) {
            int val = tso.getCurrentValue(0);
            assertTrue(val <= MAX);
            assertTrue(val >= MIN);
        }
    }
}
