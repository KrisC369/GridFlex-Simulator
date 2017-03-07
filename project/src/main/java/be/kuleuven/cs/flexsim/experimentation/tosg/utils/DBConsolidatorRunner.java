package be.kuleuven.cs.flexsim.experimentation.tosg.utils;

import be.kuleuven.cs.flexsim.persistence.MapDBConsolidator;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.AllocResultsView;
import be.kuleuven.cs.flexsim.solvers.memoization.immutableViews.ImmutableSolverProblemContextView;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Small utility for consolidating databases.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class DBConsolidatorRunner {

    public static void main(String[] args) {
        List<String> files = Lists.newArrayList("consolidation/memo1.db", "consolidation/memo2.db",
                "consolidation/memo3.db", "consolidation/memo4.db");
        MapDBConsolidator<ImmutableSolverProblemContextView, AllocResultsView> cons = new
                MapDBConsolidator(files);
        cons.consolidate();
    }
}
