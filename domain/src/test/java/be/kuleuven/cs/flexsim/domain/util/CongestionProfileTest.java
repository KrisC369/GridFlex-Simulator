package be.kuleuven.cs.flexsim.domain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;

public class CongestionProfileTest {

    private CongestionProfile profile = mock(CongestionProfile.class);
    private CongestionProfile profile2 = mock(CongestionProfile.class);
    private static String column = "test";
    private static String file = "test.csv";

    @Before
    public void setUp() throws Exception {
        profile = new CongestionProfile();
        try {
            profile = (CongestionProfile) CongestionProfile.createFromCSV(file,
                    column);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        profile2 = new CongestionProfile(new double[] { 0, 0, 0, 0 });
    }

    @Test
    public void testMean() {
        assertEquals(0, profile2.mean(), 0);
        profile2 = new CongestionProfile(new double[] { 5, 6, 7, 8 });
        assertEquals(6.5, profile2.mean(), 0.01);
    }

    @Test
    public void testMedian() {
        assertEquals(0, profile2.median(), 0);
        profile2 = new CongestionProfile(new double[] { 5, 6, 7, 8 });
        assertEquals(6.5, profile2.median(), 0.05);
    }

    @Test
    public void testStd() {
        assertEquals(0, profile2.std(), 0);
        profile2 = new CongestionProfile(new double[] { 5, 6, 7, 8 });
        assertEquals(1.29, profile2.std(), 0.05);
    }

    @Test
    public void testCopyConstructor() {
        CongestionProfile profile2;
        profile2 = (CongestionProfile) CongestionProfile
                .createFromTimeSeries(profile);
        assertEquals(Arrays.toString(profile2.values().toArray()),
                Arrays.toString(profile.values().toArray()));
    }

    @Test
    public void testChangeValue() {
        // TODO
        int index = 5;
        double value = 90;
        assertNotEquals(profile.value(index), value);
        double sum = profile.sum();
        profile.changeValue(index, value);
        assertEquals(profile.value(index), value, 0);
        assertNotEquals(sum, profile.sum());
    }

    @Test
    public void testMax() {
        double max = 7;
        profile2 = new CongestionProfile(new double[] { 0, 0, max, 0 });
        assertEquals(max, profile2.max(), 0);
    }

    @Test
    public void testSum() {
        double sum = 7;
        profile2 = new CongestionProfile(new double[] { 0, 0, sum, 0 });
        assertEquals(sum, profile2.sum(), 0);
    }

    @Test
    public void testCreateFromCSV() {
        TimeSeries profile;
        try {
            profile = CongestionProfile.createFromCSV(file, column);
            assertTrue(profile.values().size() > 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
