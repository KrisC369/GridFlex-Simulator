package be.kuleuven.cs.flexsim.experimentation;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.experimentation.RetributionFactorSensitivityRunner;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class TimingTest {

    private static final int NAGENTS = 2;
    private static final int REPITITIONS = 100;
    private static final int FACTOR = 1000;

    private static final String TAG = "TEST";

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testTiming() {
        long before1 = System.nanoTime();
        new RetributionFactorSensitivityRunner(REPITITIONS, NAGENTS, TAG)
                .execute();
        long after1 = System.nanoTime();
        new RetributionFactorSensitivityRunner(REPITITIONS, NAGENTS, TAG)
                .executeBatch();
        long after2 = System.nanoTime();
        long time1 = (after1 - before1) / FACTOR;
        long time2 = ((after2 - after1) / FACTOR);
        // assertTrue(time1 < time2);
        LoggerFactory.getLogger(TimingTest.class).info(
                "regular: " + time1 + " and Batch: " + time2);
    }
}
