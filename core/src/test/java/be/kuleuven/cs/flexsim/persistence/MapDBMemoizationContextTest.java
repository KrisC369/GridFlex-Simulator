package be.kuleuven.cs.flexsim.persistence;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MapDBMemoizationContextTest {

    private MapDBMemoizationContext<String, String> target;
    private Map<String, String> db = Maps.newLinkedHashMap();

    @Before
    public void setUp() throws Exception {
        target = MapDBMemoizationContext.createDefault();
        db.put("one", "1");
        db.put("two", "2");
        db.put("three", "3");
        db.put("four", "4");
        db.put("five", "5");
        db.put("six", "6");
        db.put("seven", "7");
        db.put("eight", "8");
        db.put("nine", "9");
        db.put("ten", "10");
    }

    @After
    public void tearDown() throws Exception {
        //target.close();
        target.resetStore();
    }

    @Test
    public void simpleInOutTest() throws Exception {
        db.entrySet().forEach((e) -> target.memoizeEntry(e.getKey(), e.getValue()));
        db.entrySet().forEach(
                (e) -> assertEquals(db.get(e.getKey()), target.getMemoizedResultFor(e.getKey())));
    }

    @Test
    public void largerInOutTest() throws Exception {
        IntStream.range(1, 1000).boxed()
                .forEach((i) -> target.memoizeEntry(i.toString(), i.toString()));
        IntStream.range(1, 1000).boxed().forEach(
                (e) -> assertEquals(e.toString(), target.getMemoizedResultFor(e.toString())));
    }

    @Test
    public void inOutSingleThread() throws Exception {
        MapDBMemoizationContext<String, String> target2 = MapDBMemoizationContext.createDefault();
        int count = 0;
        for (Map.Entry<String, String> e : db.entrySet()) {
            if (count % 2 == 0) {
                target.memoizeEntry(e.getKey(), e.getValue());
            } else {
                target2.memoizeEntry(e.getKey(), e.getValue());
            }
        }
        db.entrySet().forEach(
                (e) -> assertEquals(db.get(e.getKey()), target.getMemoizedResultFor(e.getKey())));
    }

    @Test
    public void inOutMultiThread() throws Exception {
        final int threads = 10;
        final int insertRange = 200;
        Collection<Callable<String>> runnables = Lists.newArrayList();
        IntStream.range(1, threads).forEach(i -> runnables.add(new CallableImpl(insertRange)));
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        executorService.invokeAll(runnables);
        IntStream.range(1, insertRange).boxed().forEach(
                (e) -> assertEquals(e.toString(), target.getMemoizedResultFor(e.toString())));
    }

    class CallableImpl implements Callable<String> {
        private int endRange;

        CallableImpl(int endrange) {
            this.endRange = endrange;
        }

        @Override
        public String call() throws Exception {
            MapDBMemoizationContext<String, String> target = MapDBMemoizationContext
                    .createDefault();
            IntStream.range(1, endRange).boxed().forEach(
                    (e) -> target.memoizeEntry(e.toString(), e.toString()));
            return "";
        }
    }
}