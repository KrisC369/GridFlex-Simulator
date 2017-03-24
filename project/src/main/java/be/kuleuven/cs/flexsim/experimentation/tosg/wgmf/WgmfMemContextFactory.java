package be.kuleuven.cs.flexsim.experimentation.tosg.wgmf;

import be.kuleuven.cs.flexsim.persistence.MapDBMemoizationContext;
import be.kuleuven.cs.flexsim.persistence.MemoizationContext;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import org.eclipse.jdt.annotation.Nullable;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WgmfMemContextFactory implements Serializable,
                                              Supplier<MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView>> {

    private static final long serialVersionUID = -8584004916894787633L;
    private final boolean cachingEnabled;
    private final boolean ensureCacheExists;
    private final String dbFileLocation;
    private final String dbWriteFileLocation;

    public WgmfMemContextFactory(boolean cachingEnabled, boolean ensureCacheExists,
            String dbFileLocation,
            String dbWriteFileLocation) {
        this.cachingEnabled = cachingEnabled;
        this.ensureCacheExists = ensureCacheExists;
        this.dbFileLocation = dbFileLocation;
        this.dbWriteFileLocation = dbWriteFileLocation;
    }

    @Override
    public @Nullable MemoizationContext<ImmutableSolverProblemContextView, AllocResultsView> get() {

        if (cachingEnabled) {
            MapDBMemoizationContext.Builder builder = MapDBMemoizationContext
                    .builder().setFileName(dbFileLocation)
                    .setDifferentWriteFilename(dbWriteFileLocation).ensureFileExists
                            (ensureCacheExists).appendHostnameToWriteFileName(true);
            return builder.build();
        }
        return null;
    }
}
