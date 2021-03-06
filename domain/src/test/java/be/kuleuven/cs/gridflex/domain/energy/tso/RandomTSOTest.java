package be.kuleuven.cs.gridflex.domain.energy.tso;

import be.kuleuven.cs.gridflex.domain.energy.tso.random.RandomTSO;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RandomTSOTest {
    private BalancingSignal tso = mock(BalancingSignal.class);

    private final int MIN = -44;
    private final int MAX = 70;

    @Before
    public void setUp() throws Exception {
        tso = new RandomTSO(MIN, MAX, new MersenneTwister());
    }

    @Test
    public void testSequence() {
        for (int i = 0; i < 30000; i++) {
            int val = tso.getCurrentImbalance();
            assertTrue(val <= MAX);
            assertTrue(val >= MIN);
        }
    }
}
