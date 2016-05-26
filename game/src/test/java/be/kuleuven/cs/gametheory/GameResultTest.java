package be.kuleuven.cs.gametheory;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import autovalue.shaded.com.google.common.common.collect.Lists;

public class GameResultTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testCreate() {
        GameResult res = GameResult
                .create(Lists.newArrayList(Double.valueOf(2)))
                .withDescription("test", "val");
        assertEquals(res.getDescription().get("test"), "val");
        assertEquals(res.getResults().get(0), 2, 0);
    }

    @Test
    public void testCreate2() {
        GameResult res = GameResult
                .create(Lists.newArrayList(Double.valueOf(2)))
                .withDescription("test", "val");
        GameResult res2 = res.withDescription("test2", "val2");
        assertEquals("val", res2.getDescription().get("test"));
        assertEquals("val2", res2.getDescription().get("test2"));
        assertEquals(2, res2.getResults().get(0), 0);
    }
}
