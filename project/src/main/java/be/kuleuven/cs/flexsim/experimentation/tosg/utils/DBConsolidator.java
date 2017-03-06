package be.kuleuven.cs.flexsim.experimentation.tosg.utils;

import be.kuleuven.cs.flexsim.persistence.MapDBMemoizationContext;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import com.google.common.collect.Lists;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Small utility for consolidating databases.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class DBConsolidator {

    private static final String DB_OUT_NAME = "consolidation/memoOutMerged.db";
    private static final String MAP_NAME = "map";
    private final DB dbConnection;
    private final ConcurrentMap<ImmutableSolverProblemContextView, AllocResultsView> dbAPI;
    private List<String> files;

    private DBConsolidator(List<String> files) {
        this.files = Lists.newArrayList(files);
        this.dbConnection = DBMaker.fileDB(DB_OUT_NAME).closeOnJvmShutdown().fileChannelEnable()
                .fileLockWait(Long.MAX_VALUE).fileMmapEnableIfSupported().transactionEnable()
                .executorEnable().concurrencyScale(1)
                .make();
        this.dbAPI = dbConnection.hashMap(MAP_NAME, Serializer.JAVA, Serializer
                .JAVA).createOrOpen();
    }

    private void consolidate() {
        List<MapDBMemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>>
                dbConnects = Lists.newArrayList();
        for (String file : files) {
            dbConnects.add(MapDBMemoizationContext.createDefault(file));
        }

        for (MapDBMemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> in :
                dbConnects) {
            dbAPI.putAll(in.getWholeMap());
        }
        dbConnection.commit();
        dbConnection.close();
    }

    public static void main(String[] args) {
        List<String> files = Lists.newArrayList("consolidation/memo1.db", "consolidation/memo2.db",
                "consolidation/memo3.db", "consolidation/memo4.db");
        DBConsolidator cons = new DBConsolidator(files);
        cons.consolidate();
    }
}
