package be.kuleuven.cs.flexsim.persistence;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Memoization context for concurrent read and writes to a file data store using MapDB.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *         //TODO refactor this class to be more like classic memoization api.
 */
public class MapDBMemoizationContext<E extends Serializable, R extends Serializable>
        implements MemoizationContext<E, R> {

    private static final Object LOCK = new Object();
    private static final int CONCURRENCY_SCALE = 4;
    private static final String DB_FILE = "TestFile";
    private static final String MAP_NAME = "map";
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(MapDBMemoizationContext.class);
    private DB dbConnection;
    private volatile ConcurrentMap<E, R> dbAPI;

    private MapDBMemoizationContext() {
    }

    @Override
    public void memoizeEntry(E entry, R result) {
        openForWrite();
        dbAPI.put(entry, result);
        if (logger.isDebugEnabled()) {
            logger.debug("Entry {} added to data store.", entry.toString());
        }
        commitChanges();
        close();
    }

    @Override
    public boolean hasResultFor(E entry) {
        return dbAPI.containsKey(entry);
    }

    @Override
    public R getMemoizedResultFor(E entry) {
        openForRead();
        R res = dbAPI.get(entry);
        if (logger.isDebugEnabled()) {
            logger.debug("Accessed result for entry {}", entry.toString());
        }
        close();
        return res;
    }

    private synchronized void openForWrite() {
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting opening DB connection for read-write.");
        }
        synchronized (LOCK) {
            this.dbConnection = DBMaker.fileDB(DB_FILE).closeOnJvmShutdown().fileChannelEnable()
                    .fileLockWait(Long.MAX_VALUE).fileMmapEnableIfSupported().transactionEnable()
                    .executorEnable().concurrencyScale(CONCURRENCY_SCALE)
                    .make();
            this.dbAPI = dbConnection.hashMap(MAP_NAME, Serializer.JAVA, Serializer.STRING)
                    .createOrOpen();
        }
        dbConnection.checkThreadSafe();
        if (logger.isDebugEnabled()) {
            logger.debug("DB connection opened for read-write.");
        }
    }

    private void openForRead() {
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting opening DB connection for read only.");
        }
        this.dbConnection = DBMaker.fileDB(DB_FILE).closeOnJvmShutdown().fileChannelEnable()
                .readOnly().fileLockWait(Long.MAX_VALUE).fileMmapEnableIfSupported()
                .executorEnable().concurrencyScale(CONCURRENCY_SCALE)
                .transactionEnable().make();
        this.dbAPI = dbConnection.hashMap(MAP_NAME, Serializer.JAVA, Serializer.STRING)
                .createOrOpen();
        dbConnection.checkThreadSafe();
        if (logger.isDebugEnabled()) {
            logger.debug("DB connection opened for read only.");
        }

    }

    @VisibleForTesting
    int getMemoizationTableSize() {
        openForRead();
        int size = dbAPI.size();
        if (logger.isDebugEnabled()) {
            logger.debug("Accessed table size with value {}", size);
        }
        close();
        return size;
    }

    @VisibleForTesting
    Map<E, R> getWholeMap() {
        openForRead();
        Map<E, R> tmp = Maps.newLinkedHashMap(dbAPI);
        if (logger.isDebugEnabled()) {
            logger.debug("Accessed full table.");
        }
        close();
        return tmp;
    }

    private void commitChanges() {
        dbConnection.commit();
        if (logger.isDebugEnabled()) {
            logger.debug("Changes to store comitted");
        }
    }

    public void close() {
        //        commitChanges();
        dbConnection.close();
        dbAPI = null;
        if (logger.isDebugEnabled()) {
            logger.debug("DB connection closed");
        }
    }

    void resetStore() {
        this.dbConnection = DBMaker.fileDB(DB_FILE).closeOnJvmShutdown().fileChannelEnable()
                .fileDeleteAfterClose().transactionEnable()
                .executorEnable().concurrencyScale(CONCURRENCY_SCALE)
                .make();
        if (logger.isDebugEnabled()) {
            logger.debug("Connection opened with delete file after close -option.");
        }
        close();
    }

    @VisibleForTesting
    static MapDBMemoizationContext createDefault() {
        return new MapDBMemoizationContext();
    }
}
