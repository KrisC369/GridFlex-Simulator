package be.kuleuven.cs.gridflex.domain.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static be.kuleuven.cs.gridflex.domain.util.MathUtils.trapzPos;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TrapzPosTest {
    private static int spacing = 1;
    private double x, y;
    private double out;

    public TrapzPosTest(double x, double y, double out) {
        this.x = x;
        this.y = y;
        this.out = out;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 0, 0, 0 }, { 1, 1, 1 },
                { 1, 2, 1.5 }, { 2, 1, 1.5 }, { 1, -1, 0.25 }, { -1, 1, 0.25 },
                { 0, 1, 0.5 }, { 1, 0, 0.5 }, { 0, -1, 0 }, { -1, 0, 0 },
                { -1, -5, 0 } });
    }

    @Test
    public void testCase1() {
        double res = trapzPos(x, y, spacing);
        double exp = out;
        assertEquals(exp, res, 0.01);
    }

}
