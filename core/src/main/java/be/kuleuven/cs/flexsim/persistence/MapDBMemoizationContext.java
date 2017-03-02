package be.kuleuven.cs.flexsim.persistence;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Memoization context for concurrent read and writes to a file data store using MapDB.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class MapDBMemoizationContext<E extends Serializable, R extends Serializable>
        implements MemoizationContext<E, R> {

    private static final Object LOCK = new Object();
    private static final int CONCURRENCY_SCALE = 4;
    private static final String DB_FILE = "TestFile";
    private static final String MAP_NAME = "map";
    private static Logger logger = getLogger(MapDBMemoizationContext.class);
    private DB dbConnection;
    private volatile ConcurrentMap<E, R> dbAPI;
    private final String db_filename;

    private MapDBMemoizationContext() {
        this(DB_FILE);
    }

    public MapDBMemoizationContext(String filename) {
        this.db_filename = filename;
    }

    /**
     * Save the results from this entry parameters in the memoization context.
     *
     * @param entry  The entry parameters.
     * @param result The costly calculated results.
     */
    @VisibleForTesting
    public void memoizeEntry(E entry, R result) {
        openForWrite();
        dbAPI.put(entry, result);
        logger.debug("Entry {} added to data store.", entry);
        commitChanges();
        close();
    }

    /**
     * Test if this context has results stored for this entry.
     *
     * @param entry The entry parameters.
     * @return True if precalculated results are available.
     */
    @VisibleForTesting
    public boolean hasResultFor(E entry) {
        openForRead();
        boolean res = dbAPI.containsKey(entry);
        logger.debug("Checking if entry key is present in db: {}", entry);
        close();
        return res;
    }

    /**
     * Get the results stored in the memoization context.
     *
     * @param entry The entry parameters.
     * @return The precalculated results.
     */
    @VisibleForTesting
    public R getMemoizedResultFor(E entry) {
        openForRead();
        R res = dbAPI.get(entry);
        logger.debug("Accessed result for entry {}", entry);
        close();
        return res;
    }

    @Override
    public R testAndCall(E entry, Supplier<R> calculationFu) {
        openForRead();
        R res = null;
        if (dbAPI.containsKey(entry)) {
            res = getMemoizedResultFor(entry);
        }
        close();
        if (res != null) {
            return res;
        }
        res = calculationFu.get();
        openForWrite();
        dbAPI.put(entry, res);
        commitChanges();
        close();
        return res;
    }

    private synchronized void openForWrite() {
        logger.debug("Attempting opening DB connection for read-write.");
        synchronized (LOCK) {
            this.dbConnection = DBMaker.fileDB(db_filename).closeOnJvmShutdown().fileChannelEnable()
                    .fileLockWait(Long.MAX_VALUE).fileMmapEnableIfSupported().transactionEnable()
                    .executorEnable().concurrencyScale(CONCURRENCY_SCALE)
                    .make();
            this.dbAPI = dbConnection.hashMap(MAP_NAME, Serializer.JAVA, Serializer
                    .JAVA).createOrOpen();
        }
        dbConnection.checkThreadSafe();
        logger.debug("DB connection opened for read-write.");
    }

    private void openForRead() {
        logger.debug("Attempting opening DB connection for read only.");
        this.dbConnection = DBMaker.fileDB(db_filename).closeOnJvmShutdown().fileChannelEnable()
                .readOnly().fileLockWait(Long.MAX_VALUE).fileMmapEnableIfSupported()
                .executorEnable().concurrencyScale(CONCURRENCY_SCALE)
                .transactionEnable().make();
        this.dbAPI = dbConnection.hashMap(MAP_NAME, Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();
        dbConnection.checkThreadSafe();
        logger.debug("DB connection opened for read only.");

    }

    @VisibleForTesting
    int getMemoizationTableSize() {
        openForRead();
        int size = dbAPI.size();
        logger.debug("Accessed table size with value {}", size);
        close();
        return size;
    }

    @VisibleForTesting
    Map<E, R> getWholeMap() {
        openForRead();
        Map<E, R> tmp = Maps.newLinkedHashMap(dbAPI);
        logger.debug("Accessed full table.");
        close();
        return tmp;
    }

    private void commitChanges() {
        dbConnection.commit();
        logger.debug("Changes to store comitted");
    }

    public void close() {
        //        commitChanges();
        dbConnection.close();
        dbAPI = null;
        logger.debug("DB connection closed");
    }

    private void ensureFileInit() {
        synchronized (LOCK) {
            openForWrite();
            commitChanges();
            close();
        }
    }

    public void resetStore() {
        this.dbConnection = DBMaker.fileDB(db_filename).closeOnJvmShutdown().fileChannelEnable()
                .fileDeleteAfterClose().transactionEnable()
                .executorEnable().concurrencyScale(CONCURRENCY_SCALE)
                .make();
        logger.debug("Connection opened with delete file after close -option.");
        close();
    }

    @VisibleForTesting
    public static <E extends Serializable, R extends Serializable> MapDBMemoizationContext<E, R>
    createDefault(String filename) {
        return new MapDBMemoizationContext(filename);
    }

    public static <E extends Serializable, R extends Serializable> MapDBMemoizationContext<E, R>
    createDefaultEnsureFileExists(String filename) {
        MapDBMemoizationContext mapDBMemoizationContext = new MapDBMemoizationContext(filename);
        mapDBMemoizationContext.ensureFileInit();
        return mapDBMemoizationContext;
    }
}
