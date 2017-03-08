package be.kuleuven.cs.flexsim.persistence;

import com.google.common.collect.Lists;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class MapDBConsolidator<E extends Serializable, R extends Serializable> {

    private static final String DB_OUT_NAME = "consolidation/memoOutMerged.db";
    private static final String MAP_NAME = "map";
    private final DB dbConnection;
    private final ConcurrentMap<E, R> dbAPI;
    private List<String> files;
    private static Logger logger = LoggerFactory.getLogger(MapDBConsolidator.class);

    public MapDBConsolidator(List<String> files) {
        this(files, DB_OUT_NAME);
    }

    public MapDBConsolidator(List<String> files, String outFile) {
        this.files = Lists.newArrayList(files);
        this.dbConnection = DBMaker.fileDB(outFile).closeOnJvmShutdown().fileChannelEnable()
                .fileLockWait(Long.MAX_VALUE).fileMmapEnableIfSupported().transactionEnable()
                .executorEnable().concurrencyScale(1).make();
        this.dbAPI = dbConnection.hashMap(MAP_NAME, Serializer.JAVA, Serializer
                .JAVA).createOrOpen();
    }

    public void consolidate() {
        logger.info("Starting consolidation process.");
        List<MapDBMemoizationContext<E, R>>
                dbConnects = Lists.newArrayList();
        for (String file : files) {
            dbConnects.add(MapDBMemoizationContext.createDefault(file));
        }

        for (MapDBMemoizationContext<E, R> in :
                dbConnects) {
            logger.debug("Getting map for {}", in);
            dbAPI.putAll(in.getWholeMap());
            dbConnection.commit();
        }
        dbConnection.close();
    }
}
