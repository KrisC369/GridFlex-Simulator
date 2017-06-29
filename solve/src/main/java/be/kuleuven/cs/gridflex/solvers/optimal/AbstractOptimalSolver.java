package be.kuleuven.cs.gridflex.solvers.optimal;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.solvers.data.AllocResults;
import com.google.common.collect.Lists;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.MpSolver;
import net.sf.jmpi.solver.cplex.SolverCPLEX;
import net.sf.jmpi.solver.gurobi.SolverGurobi;
import org.eclipse.jdt.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Abstract solvers that uses flexibility providers to solve the optimal allocation given the
 * constraints and variables provided by the subclass.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class AbstractOptimalSolver implements Solver<AllocResults> {
    /**
     * The number of discrete units per hour. For this solvers the discretization step is in 15
     * min blocks.
     */
    public static final double STEPS_PER_HOUR = 4;
    private boolean verbose = false;
    private final FlexAllocProblemContext context;
    private final Solver solver;

    /**
     * Default constructor
     */
    protected AbstractOptimalSolver(FlexAllocProblemContext context, Solver s) {
        this.context = context;
        this.solver = s;
    }

    /**
     * Set the solvers to verbose solving output.
     *
     * @param b true if verbose output is wanted.
     */
    public void setVerboseSolving(final boolean b) {
        this.verbose = b;
    }

    @Override
    public AllocResults solve() {
        doSolve();
        return getSolution();
    }

    private void doSolve() {
        final MpSolver s = getSolver().getInstance();
        s.add(getProblem());
        s.setVerbose(this.verbose ? 1 : 0);
        final MpResult result = s.solve();
        processResults(Optional.ofNullable(result));
    }

    /**
     * This method is a callback with optional results from solvers after solving.
     *
     * @param result Optional results. Optional is empty if solution is infeasible for the given
     *               problem.
     */
    protected abstract void processResults(@NonNull Optional<MpResult> result);

    /**
     * @return An solvers instance.
     */
    private Solver getSolver() {
        return solver;
    }

    /**
     * @return The MP Problem instance, fully configured.
     */
    protected abstract MpProblem getProblem();

    /**
     * @return The results.
     */

    protected abstract AllocResults getSolution();

    /**
     * @return the registered flex providers.
     */
    public final List<FlexibilityProvider> getProviders() {
        return Collections.unmodifiableList(Lists.newArrayList(context.getProviders()));
    }

    /**
     * Enum for the supported solvers engines to use.
     * Either use Gurobi or Cplex.
     */
    public enum Solver {
        GUROBI, CPLEX, DUMMY;

        /**
         * @return a new MpSolver instance.
         */
        MpSolver getInstance() {
            if (GUROBI == this) {
                return new SolverGurobi();
            } else if (CPLEX == this) {
                return new SolverCPLEX();
            } else {
                return new DummyMIP();
            }
        }
    }

    private static class DummyMIP implements MpSolver {

        @Override
        public void add(MpProblem problem) {
        }

        @Override
        public MpResult solve() {
            return new DummyMpResult();
        }

        @Override
        public void setTimeout(int value) {
        }

        @Override
        public void setVerbose(int value) {
        }

        private static class DummyMpResult implements MpResult {
            @Override
            public Number getObjective() {
                return 0;
            }

            @Override
            public boolean getBoolean(Object var) {
                return false;
            }

            @Override
            public Number get(Object var) {
                return 0;
            }

            @Override
            public void put(Object var, Number value) {
            }

            @Override
            public Boolean containsVar(Object var) {
                return true;
            }
        }
    }
}
