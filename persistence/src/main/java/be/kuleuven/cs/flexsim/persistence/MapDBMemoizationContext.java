package be.kuleuven.cs.flexsim.persistence;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;

import java.io.File;
import java.io.Serializable;
import java.net.UnknownHostException;
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
    private static final int CONCURRENCY_SCALE = 64;
    private static final String DB_FILE_4_READ = "TestFile";
    private static final String DB_FILE_4_WRITE = "TestFile";
    private static final String MAP_NAME = "map";
    private static Logger logger = getLogger(MapDBMemoizationContext.class);
    private StoreAccessWrapper<E, R> readDB;
    private StoreAccessWrapper<E, R> writeDB;

    public MapDBMemoizationContext(String filename_r, String filename_w, boolean uniqueWriteFile) {
        this.readDB = new StoreAccessWrapper<>(true, filename_r);

        String tmpName = filename_w;
        if (uniqueWriteFile) {
            String name = "unknown";
            try {
                name = java.net.InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            tmpName += "_" + name;
        }
        this.writeDB = new StoreAccessWrapper<>(false, tmpName);
        writeDir(filename_w);
        writeDir(filename_r);

    }

    private static void writeDir(String fullPath) {
        String[] dirs = fullPath.split("/");
        String dirPath = "";
        for (int i = 0; i < dirs.length - 1; i++) {
            dirPath += dirs[i] + "/";
        }
        File f = new File(dirPath);
        f.mkdirs();
    }

    /**
     * Save the results from this entry parameters in the memoization context.
     *
     * @param entry  The entry parameters.
     * @param result The costly calculated results.
     */
    @VisibleForTesting
    public void memoizeEntry(E entry, R result) {
        ConcurrentMap<E, R> erConcurrentMap = writeDB.openAndGetStore();
        erConcurrentMap.put(entry, result);
        logger.debug("Entry {} added to data store.", entry);
        writeDB.commitChanges();
        writeDB.close();
    }

    /**
     * Test if this context has results stored for this entry.
     *
     * @param entry The entry parameters.
     * @return True if precalculated results are available.
     */
    @VisibleForTesting
    public boolean hasResultFor(E entry) {
        ConcurrentMap<E, R> erConcurrentMap = readDB.openAndGetStore();
        boolean res = erConcurrentMap.containsKey(entry);
        logger.debug("Checking if entry key is present in db: {}", entry);
        readDB.close();
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
        ConcurrentMap<E, R> erConcurrentMap = readDB.openAndGetStore();
        R res = erConcurrentMap.get(entry);
        logger.debug("Accessed result for entry {}", entry);
        readDB.close();
        return res;
    }

    @Override
    public R testAndCall(E entry, Supplier<R> calculationFu, boolean updateCache) {
        logger.debug("Attempting memoization.");
        R res;
        try {
            res = getMemoizedResultFor(entry);
            if (res != null) {
                logger.debug("Memoized result found. No need to calculate.");
                return res;
            }
            logger.debug("No memoized result found. Calculating actual result.");
        } catch (DBException.VolumeIOError e) {
            logger.warn("No memoization file to read, skipping lookup");
        }
        res = calculationFu.get();
        if (updateCache) {
            logger.debug("Memoizing calculated result.");
            memoizeEntry(entry, res);
        }
        return res;
    }

    @VisibleForTesting
    int getMemoizationTableSize() {
        ConcurrentMap<E, R> erConcurrentMap = readDB.openAndGetStore();
        int size = erConcurrentMap.size();
        logger.debug("Accessed table size with value {}", size);
        readDB.close();
        return size;
    }

    @VisibleForTesting
    public Map<E, R> getWholeMap() {
        ConcurrentMap<E, R> erConcurrentMap = readDB.openAndGetStore();
        Map<E, R> tmp = Maps.newLinkedHashMap(erConcurrentMap);
        logger.debug("Accessed full table.");
        readDB.close();
        return tmp;
    }

    private void ensureFileInit() {
        //        readDB.forceCreation();
        writeDB.forceCreation();
    }

    public void resetStore() {
        synchronized (LOCK) {
            readDB.forceReset();
            writeDB.forceReset();
        }
    }

    @Override
    public String toString() {
        return "MapDBMemoizationContext{" +
                "readDB=" + readDB +
                ", writeDB=" + writeDB +
                '}';
    }

    boolean isClosed() {
        return readDB.isClosed() && writeDB.isClosed();
    }

    //    @VisibleForTesting
    //    @Deprecated
    //    public static <E extends Serializable, R extends Serializable>
    // MapDBMemoizationContext<E, R>
    //    createDefault(String filename) {
    //        return new Builder().setFileName(filename).setDifferentWriteFilename(filename)
    //                .build();
    //    }
    //
    //    @Deprecated
    //    public static <E extends Serializable, R extends Serializable>
    // MapDBMemoizationContext<E, R>
    //    createUniqueTwoFile(String filename_r, String filename_w) {
    //        return createTwoFile(filename_r, filename_w, true, false);
    //    }
    //
    //    @Deprecated
    //    public static <E extends Serializable, R extends Serializable>
    // MapDBMemoizationContext<E, R>
    //    createDefaultEnsureFileExists(String filename) {
    //        return createTwoFileEnsureFileExists(filename, filename, false);
    //    }
    //
    //    @Deprecated
    //    public static <E extends Serializable, R extends Serializable>
    // MapDBMemoizationContext<E, R>
    //    createTwoFile(String filename_r, String filename_w, boolean unique, boolean ensure) {
    //        MapDBMemoizationContext<E, R> mapDBMemoizationContext = new Builder()
    //                .setFileName(filename_r).setDifferentWriteFilename(filename_w)
    //                .appendHostnameToWriteFileName()
    //                .build();
    //        if (ensure) {
    //            mapDBMemoizationContext.ensureFileInit();
    //        }
    //        return mapDBMemoizationContext;
    //    }
    //
    //    @Deprecated
    //    public static <E extends Serializable, R extends Serializable>
    // MapDBMemoizationContext<E, R>
    //    createTwoFileEnsureFileExists(String filename_r, String filename_w, boolean unique) {
    //        return createTwoFile(filename_r, filename_w, unique, true);
    //    }
    //
    //    @Deprecated
    //    public static <E extends Serializable, R extends Serializable>
    // MapDBMemoizationContext<E, R>
    //    createTwoFileEnsureFileExistsWUnique(String filename_r, String filename_w) {
    //        return createTwoFileEnsureFileExists(filename_r, filename_w, true);
    //    }

    public static MapDBMemoizationContext.Builder builder() {
        return new Builder();
    }

    private static class StoreAccessWrapper<E, R> {
        private final boolean readonly;
        private DB dbConnection;
        private volatile ConcurrentMap<E, R> persistedMap;
        private final String db_filename;
        private final boolean deleteAfter;

        private StoreAccessWrapper(boolean readonly, String db_filename, boolean deleteAfter) {
            this.readonly = readonly;
            this.db_filename = db_filename;
            this.deleteAfter = deleteAfter;
        }

        private StoreAccessWrapper(boolean readonly, String db_filename) {
            this(readonly, db_filename, false);
        }

        private void open() {
            logger.debug(
                    "Attempting opening DB connection for " + (readonly ?
                            "read only." :
                            "writing."));
            synchronized (LOCK) {
                DBMaker.Maker maker = DBMaker.fileDB(db_filename).closeOnJvmShutdown()
                        .fileChannelEnable().fileLockWait(Long.MAX_VALUE)
                        .fileMmapEnableIfSupported()
                        .executorEnable().concurrencyScale(CONCURRENCY_SCALE)
                        .transactionEnable();
                if (readonly) {
                    maker.readOnly();
                }
                if (deleteAfter) {
                    maker.fileDeleteAfterClose();
                }
                this.dbConnection = maker.make();
                this.persistedMap = dbConnection.hashMap(MAP_NAME, Serializer.JAVA, Serializer.JAVA)
                        .createOrOpen();
            }
            logger.debug("DB connection opened for " + (readonly ? "read only." : "writing."));
        }

        void commitChanges() {
            dbConnection.commit();
            logger.debug("Changes to store comitted");
        }

        void close() {
            dbConnection.close();
            persistedMap = null;
            logger.debug("DB connection closed");
        }

        void forceCreation() {
            StoreAccessWrapper<E, R> storeAccessWrapper = new StoreAccessWrapper<>(false,
                    db_filename);
            storeAccessWrapper.open();
            storeAccessWrapper.commitChanges();
            storeAccessWrapper.close();
        }

        void forceReset() {
            try {
                StoreAccessWrapper<E, R> storeAccessWrapper = new StoreAccessWrapper<>(true,
                        db_filename, true);
                storeAccessWrapper.open();
                storeAccessWrapper.commitChanges();
                storeAccessWrapper.close();
            } catch (DBException.VolumeIOError e) {
                //means file has already been delete. We're done.
            }
        }

        ConcurrentMap<E, R> openAndGetStore() {
            open();
            return persistedMap;
        }

        boolean isClosed() {
            return dbConnection == null || dbConnection.isClosed();
        }

        @Override
        public String toString() {
            return "StoreAccessWrapper{" +
                    "db_filename='" + db_filename + '\'' +
                    ", readonly=" + readonly +
                    '}';
        }
    }

    /**
     * Builder for map DB memoziation context.
     */
    public static class Builder {
        private String filename_r = DB_FILE_4_READ;
        private String filename_w = DB_FILE_4_WRITE;
        private boolean uniqueWriteFile = false;
        private boolean ensureFilesExist = false;

        /**
         * Set the filename for this mem context.
         * Calling this will set writing and reading pointer to the same file.
         *
         * @param filename the file name to read from and write to.
         * @return this builder.
         */
        public Builder setFileName(String filename) {
            this.filename_r = filename;
            this.filename_w = filename;
            return this;
        }

        /**
         * Optionally set a different file to write to than the read file.
         *
         * @param filename_w the filename to write to.
         * @return this builder.
         */
        public Builder setDifferentWriteFilename(String filename_w) {
            this.filename_w = filename_w;
            return this;
        }

        /**
         * Make the write files unique based on the hostname of the machine.
         * The default is false.
         *
         * @return this builder.
         */
        public Builder appendHostnameToWriteFileName() {
            return appendHostnameToWriteFileName(true);
        }

        /**
         * Make the write files unique based on the hostname of the machine.
         * The default is false.
         *
         * @param unique True if file should be unique across hosts.
         * @return this builder.
         */
        public Builder appendHostnameToWriteFileName(boolean unique) {
            this.uniqueWriteFile = unique;
            return this;
        }

        /**
         * Ensure the write file exists before read/writing to them.
         * The default is false.
         *
         * @return this builder
         */
        public Builder ensureFileExists() {
            return ensureFileExists(true);
        }

        /**
         * Ensure the write file exists before read/writing to them.
         * The default is false.
         *
         * @param ensure true if file should be created.
         * @return this builder
         */
        public Builder ensureFileExists(boolean ensure) {
            this.ensureFilesExist = ensure;
            return this;
        }

        /**
         * Build the mem context
         *
         * @return The fully built memoization context.
         */
        public <E extends Serializable, R extends Serializable> MapDBMemoizationContext<E, R>
        build() {
            MapDBMemoizationContext<E, R> mapDBMemoizationContext = new MapDBMemoizationContext<>(
                    filename_r, filename_w, uniqueWriteFile);
            if (ensureFilesExist) {
                mapDBMemoizationContext.ensureFileInit();
            }
            return mapDBMemoizationContext;
        }
    }
}
