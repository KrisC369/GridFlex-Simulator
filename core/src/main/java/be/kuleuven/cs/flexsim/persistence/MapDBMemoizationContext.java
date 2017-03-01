package be.kuleuven.cs.flexsim.persistence;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * Memoization context for concurrent read and writes to a file data store using MapDB.
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MapDBMemoizationContext<E extends Serializable, R extends Serializable>
        implements MemoizationContext<E, R> {

    private static final int CONCURRENCY_SCALE = 90;
    private static final String DB_FILE = "TestFile";
    private static final String MAP_NAME = "map";
    private DB dbConnection;
    private ConcurrentMap<E, R> dbAPI;

    private MapDBMemoizationContext() {
        //openForWrite();
    }

    @Override
    public void memoizeEntry(E entry, R result) {
        openForWrite();
        dbAPI.put(entry, result);
        dbConnection.commit();
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
        close();
        return res;
    }

    private void openForWrite() {
        this.dbConnection = DBMaker.fileDB(DB_FILE).closeOnJvmShutdown().fileChannelEnable().fileLockWait()
                .executorEnable().concurrencyScale(CONCURRENCY_SCALE).transactionEnable().make();
        this.dbAPI = dbConnection.hashMap(MAP_NAME, Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();
    }

    private void openForRead() {
        this.dbConnection = DBMaker.fileDB(DB_FILE).closeOnJvmShutdown().fileChannelEnable()
                .readOnly()
                .executorEnable().concurrencyScale(CONCURRENCY_SCALE).transactionEnable().make();
        this.dbAPI = dbConnection.hashMap(MAP_NAME, Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();
    }

    public void close() {
        dbConnection.commit();
        dbConnection.close();
    }

    void resetStore() {
        this.dbConnection = DBMaker.fileDB(DB_FILE).closeOnJvmShutdown().fileChannelEnable()
                .fileDeleteAfterClose()
                .executorEnable().concurrencyScale(CONCURRENCY_SCALE).transactionEnable().make();
        close();
    }

    static MapDBMemoizationContext createDefault() {
        return new MapDBMemoizationContext();
    }
}
