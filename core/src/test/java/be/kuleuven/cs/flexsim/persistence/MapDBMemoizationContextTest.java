package be.kuleuven.cs.flexsim.persistence;

import com.google.auto.value.AutoValue;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MapDBMemoizationContextTest {

    private MapDBMemoizationContext<String, String> target;
    private MapDBMemoizationContext<KeyObject, ValueObject> target2;
    private Map<String, String> db = Maps.newLinkedHashMap();
    private static final Logger logger = LoggerFactory.getLogger(MapDBMemoizationContextTest.class);
    private static final String NAME_DB = "TestFile";

    @Before
    public void setUp() throws Exception {
        target = MapDBMemoizationContext.createDefaultEnsureFileExists(NAME_DB);
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

    @AfterClass
    public static void cleanup() throws Exception {
        MapDBMemoizationContext<String, String> target = MapDBMemoizationContext
                .createDefault(NAME_DB);
        target.resetStore();
        MapDBMemoizationContext<KeyObject, ValueObject> target2 = MapDBMemoizationContext
                .createDefault("testObj.db");
        target.resetStore();
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
        MapDBMemoizationContext<String, String> target2 = MapDBMemoizationContext
                .createDefault(NAME_DB);
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
    public void hasEntryTest() throws Exception {
        reset();
        int count = 0;
        for (Map.Entry<String, String> e : db.entrySet()) {
            target.memoizeEntry(e.getKey(), e.getValue());
        }
        db.entrySet().forEach(
                (e) -> assertTrue(target.hasResultFor(e.getKey())));
    }

    @Test
    public void inOutMultiThread() throws Exception {
        reset();
        parallelTestImpl(5, 8, 20, true, NAME_DB);
    }

    public static void parallelTestImpl(int offset, final int threads, final int insertRange,
            boolean doRangeCheck, String dbfilename)
            throws InterruptedException {
        MapDBMemoizationContext<String, String> target = MapDBMemoizationContext
                .createDefault(dbfilename);
        final CountDownLatch latch = new CountDownLatch(threads);
        Collection<Callable<String>> runnables = Lists.newArrayList();
        IntStream.range(0, threads)
                .forEach(i -> runnables
                        .add(new CallableImpl(latch, offset + ((i) * insertRange), insertRange,
                                dbfilename)));
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

    @Test
    public void testObjectSerialization() {
        MapDBMemoizationContext<KeyObject, ValueObject> target2 = MapDBMemoizationContext
                .createDefaultEnsureFileExists("testObj.db");
        ArrayList<Double> list = Lists.newArrayList();
        list.add((double) 2);
        list.add((double) 2);
        list.add((double) 2);
        list.add((double) 2);

        KeyObject key = KeyObject.create(2, "TestK", list);
        ListMultimap<KeyObject, Boolean> map = LinkedListMultimap.create();
        map.put(key, Boolean.TRUE);
        map.put(key, Boolean.FALSE);
        map.put(key, Boolean.TRUE);

        ValueObject value = ValueObject.create(2, "TestV", list, map);
        target2.memoizeEntry(key, value);
        ValueObject memoizedResultFor = target2.getMemoizedResultFor(key);
        assertEquals(value, memoizedResultFor);
    }

    private static void doMemo(Integer i, MapDBMemoizationContext target) {
        target.memoizeEntry(i.toString(), i.toString());
    }

    @Test
    public void testMemoizationCall() {
        final CountDownLatch countDownLatch = new CountDownLatch(3);
        target.testAndCall(db.get("one"), () -> {
            logCall(countDownLatch);
            return db.get("one");
        });
        assertEquals(2, countDownLatch.getCount(), 0);

        target.testAndCall(db.get("one"), () -> {
            logCall(countDownLatch);
            return db.get("one");
        });
        assertEquals(2, countDownLatch.getCount(), 0);
        assertTrue(target.isClosed());
    }

    void logCall(CountDownLatch l) {
        l.countDown();
    }

    static class CallableImpl implements Callable<String> {
        private final CountDownLatch latch;
        private final int endRange;
        private final int start;
        private final String dbfilename;

        CallableImpl(CountDownLatch latch, int start, int endrange, String dbfilename) {
            this.latch = latch;
            this.endRange = endrange;
            this.start = start;
            this.dbfilename = dbfilename;
        }

        @Override
        public String call() throws Exception {
            try {
                MapDBMemoizationContext<String, String> target = MapDBMemoizationContext
                        .createDefault(dbfilename);
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

    @AutoValue
    abstract static class KeyObject implements Serializable {
        private static final long serialVersionUID = 8555103073003453496L;

        public abstract double getVal1();

        public abstract String getVal2();

        public abstract List<Double> getVal3();

        public static KeyObject create(double newVal1, String newVal2,
                List<Double> newVal3) {
            return new AutoValue_MapDBMemoizationContextTest_KeyObject(newVal1, newVal2, newVal3);
        }
    }

    @AutoValue
    abstract static class ValueObject implements Serializable {

        private static final long serialVersionUID = 4794871360396143260L;

        public abstract double getVal1();

        public abstract String getVal2();

        public abstract List<Double> getVal3();

        public abstract ListMultimap<KeyObject, Boolean> getVal4();

        public static ValueObject create(double newVal1, String newVal2,
                List<Double> newVal3, ListMultimap<KeyObject, Boolean> val4) {
            return new AutoValue_MapDBMemoizationContextTest_ValueObject(newVal1, newVal2, newVal3,
                    val4);
        }

    }

}