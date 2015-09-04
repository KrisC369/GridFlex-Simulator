package be.kuleuven.cs.flexsim.domain.util;

import static org.junit.Assert.assertEquals;
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
    }

    @Test
    public void testMean() {
        assertEquals(0, profile.mean(), 0);
        profile = new CongestionProfile(new double[] { 5, 6, 7, 8 });
        assertEquals(6.5, profile.mean(), 0.01);
    }

    @Test
    public void testMedian() {
        assertEquals(0, profile.median(), 0);
        profile = new CongestionProfile(new double[] { 5, 6, 7, 8 });
        assertEquals(6.5, profile.median(), 0.05);
    }

    @Test
    public void testStd() {
        assertEquals(0, profile.std(), 0);
        profile = new CongestionProfile(new double[] { 5, 6, 7, 8 });
        assertEquals(1.29, profile.std(), 0.05);
    }

    @Test
    public void testCopyConstructor() {
        CongestionProfile profile2;
        profile2 = (CongestionProfile) CongestionProfile
                .createFromTimeSeries(profile);
        assertEquals(Arrays.toString(profile2.values()),
                Arrays.toString(profile.values()));

    }

    @Test
    public void testCreateFromCSV() {
        TimeSeries profile;
        try {
            profile = CongestionProfile.createFromCSV(file, column);
            assertTrue(profile.values().length > 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
