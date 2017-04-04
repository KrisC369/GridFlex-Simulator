package be.kuleuven.cs.gametheory;

import be.kuleuven.cs.gametheory.results.GameResult;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GameResultTest {
    private GameResult<List<Double>> res;

    @Before
    public void setUp() throws Exception {
        res = GameResult
                .create(Lists.newArrayList(Double.valueOf(2)))
                .withDescription("test", "val");
    }

    @Test
    public void testCreate() {
        assertEquals(res.getDescription().get("test"), "val");
        assertEquals(res.getResults().get(0), 2, 0);
    }

    @Test
    public void testCreate2() {
        GameResult<List<Double>> res2 = res.withDescription("test2", "val2");
        assertEquals("val", res2.getDescription().get("test"));
        assertEquals("val2", res2.getDescription().get("test2"));
        assertEquals(2, res2.getResults().get(0), 0);
    }
}
