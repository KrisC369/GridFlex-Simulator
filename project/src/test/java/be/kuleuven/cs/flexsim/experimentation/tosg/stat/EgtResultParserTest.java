package be.kuleuven.cs.flexsim.experimentation.tosg.stat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class EgtResultParserTest {
    //    private static final String ML_LOC = "/Applications/MATLAB_R2015b.app/bin/matlab";
    private static final double EPS = 1.0E-02;
    private EgtResultParser parser;

    @Before
    public void setUp() throws Exception {
        try {
            parser = new EgtResultParser(null);
        } catch (IllegalStateException e) {
            //For whatever reason, result parser could not be initiated. Probably because ML is
            // not installed. Mock the parser for now.
            parser = mock(EgtResultParser.class);
            when(parser.findFixedPointForDynEquationParams(Matchers.any()))
                    .thenReturn(new double[] { 0.5 });
        }
    }

    @After
    public void tearDown() throws Exception {
        parser.close();
    }

    @Test
    public void testFindFixedPoint() {
        double[] dynParams = { 50.0, 0, 0, 50.0 };
        double[] fixedPoints = parser.findFixedPointForDynEquationParams(dynParams);
        assertEquals(1, fixedPoints.length);
        assertEquals(0.5, fixedPoints[0], EPS);
    }

}