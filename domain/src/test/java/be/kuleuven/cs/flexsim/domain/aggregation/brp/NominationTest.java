package be.kuleuven.cs.flexsim.domain.aggregation.brp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class NominationTest {
    private static final long target = 243;
    private static final long result = 100;
    private Nomination nomination = mock(Nomination.class);

    @Before
    public void setUp() throws Exception {
        nomination = Nomination.create(target, result);
    }

    @Test
    public void testGenerationOrder() {
        assertEquals(target, nomination.getTargetImbalanceVolume(), 0);
        assertEquals(result, nomination.getRemediedImbalanceVolume(), 0);
    }

}
