package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
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
 * Abstract solver that uses flexibility providers to solve the optimal allocation given the
 * constraints and variables provided by the subclass.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class AbstractOptimalSolver implements SimulationComponent {
    /**
     * The number of discrete units per hour. For this solver the discretization step is in 15
     * min blocks.
     */
    public static final int STEPS_PER_HOUR = 4;
    private final List<FlexProvider> providers;
    private boolean verbose = false;

    /**
     * Default constructor
     */
    protected AbstractOptimalSolver() {
        providers = Lists.newArrayList();
    }

    /**
     * Register a flex provider to participate in the flex allocation process.
     *
     * @param p The provider to register
     */
    public void registerFlexProvider(final FlexProvider p) {
        providers.add(p);
    }

    /**
     * Set the solver to verbose solving output.
     *
     * @param b true if verbose output is wanted.
     */
    public void setVerboseSolving(final boolean b) {
        this.verbose = b;
    }

    @Override
    public void afterTick(final int t) {
        //Nothing to do
    }

    @Override
    public void tick(final int t) {
        final MpSolver s = getSolver().getInstance();
        s.add(getProblem());
        s.setVerbose(this.verbose ? 1 : 0);
        final MpResult result = s.solve();
        processResults(java.util.Optional.of(result));
    }

    /**
     * This method is a callback with optional results from solver after solving.
     *
     * @param result Optional results. Optional is empty if solution is infeasible for the given
     *               problem.
     */
    protected abstract void processResults(@NonNull Optional<MpResult> result);

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    @Override
    public void initialize(final SimulationContext context) {
        //Nothing to do
    }

    /**
     * @return An solver instance.
     */
    protected abstract Solver getSolver();

    /**
     * @return The MP Problem instance, fully configured.
     */
    protected abstract MpProblem getProblem();

    /**
     * @return The results.
     */
    public abstract AllocResults getResults();

    /**
     * @return the registered flex providers.
     */
    protected final List<FlexProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    /**
     * Enum for the supported solver engines to use.
     * Either use Gurobi or Cplex.
     */
    public enum Solver {
        GUROBI, CPLEX;

        /**
         * @return a new MpSolver instance.
         */
        MpSolver getInstance() {
            if (GUROBI == this) {
                return new SolverGurobi();
            } else {
                return new SolverCPLEX();
            }
        }
    }
}
