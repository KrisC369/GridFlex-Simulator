package be.kuleuven.cs.flexsim.persistence;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.igormaznitsa.jute.annotations.JUteTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
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
    private static final Logger logger = LoggerFactory.getLogger(MapDBMemoizationContextTest.class);

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
        //target.resetStore();
    }

    private void reset() {
        target.resetStore();
    }

    @Test
    public void simpleInOutTest() throws Exception {
        reset();
        db.entrySet().forEach((e) -> target.memoizeEntry(e.getKey(), e.getValue()));
        db.entrySet().forEach(
                (e) -> assertEquals(db.get(e.getKey()), target.getMemoizedResultFor(e.getKey())));
    }

    @Test
    public void largerInOutTest() throws Exception {
        reset();
        IntStream.range(1, 1000).boxed()
                .forEach((i) -> target.memoizeEntry(i.toString(), i.toString()));
        IntStream.range(1, 1000).boxed().forEach(
                (e) -> assertEquals(e.toString(), target.getMemoizedResultFor(e.toString())));
    }

    @Test
    public void inOutSingleThread() throws Exception {
        reset();
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

    @JUteTest(order = 1, jvm = "java", printConsole = true)
    public void parallell_1_JuteTest() throws Exception {
        parallelTestImpl(0, 4, 250, false);

    }

    @JUteTest(order = 1, jvm = "java", printConsole = true)
    public void parallell_2_JuteTest() throws Exception {
        parallelTestImpl(1000, 4, 250, false);

    }

    @JUteTest(order = 1, jvm = "java", printConsole = true)
    public void parallell_3_JuteTest() throws Exception {
        parallelTestImpl(2000, 4, 250, false);
    }

    @Test
    public void inOutMultiThread() throws Exception {
        reset();
        parallelTestImpl(5, 8, 20, true);
    }

    public void parallelTestImpl(int offset, final int threads, final int insertRange,
            boolean doRangeCheck)
            throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(threads);
        Collection<Callable<String>> runnables = Lists.newArrayList();
        IntStream.range(0, threads)
                .forEach(i -> runnables
                        .add(new CallableImpl(latch, offset + ((i) * insertRange), insertRange)));
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        executorService.invokeAll(runnables);

        if (doRangeCheck) {
            Map<String, String> wholeMap = target.getWholeMap();
            //        System.out.println(wholeMap);
            assertEquals((threads) * insertRange, wholeMap.size());
            assertEquals((threads) * insertRange, target.getMemoizationTableSize(), 0);
        }
        for (int i = 0 + offset; i < offset + ((threads) * insertRange); i++) {
            assertEquals(((Integer) i).toString(),
                    target.getMemoizedResultFor(((Integer) i).toString()));
        }
    }

    class CallableImpl implements Callable<String> {
        private CountDownLatch latch;
        private int endRange;
        private int start;

        CallableImpl(CountDownLatch latch, int start, int endrange) {
            this.latch = latch;
            this.endRange = endrange;
            this.start = start;
        }

        @Override
        public String call() throws Exception {
            try {
                MapDBMemoizationContext<String, String> target = MapDBMemoizationContext
                        .createDefault();
                IntStream.range(start, start + endRange).boxed().forEach(
                        (e) -> doMemo(e, target));
                latch.countDown();
                logger.debug("Thread done inserting. Latch now at {}", latch.getCount());
            } catch (Exception e) {
                logger.error("Exception caught", e);
            }
            return "";
        }
    }

    private static void doMemo(Integer i, MapDBMemoizationContext target) {
        target.memoizeEntry(i.toString(), i.toString());
    }
}