package be.kuleuven.cs.gridflex.solvers.dummy;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.solvers.data.AllocResults;
import be.kuleuven.cs.gridflex.solvers.optimal.AbstractOptimalSolver;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;

import java.util.Optional;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class SolverDummy extends AbstractOptimalSolver {
    private final int profileLength;
    private AllocResults results;

    /**
     * Default constructor
     *
     * @param context The problem context to (not) work with.
     */
    public SolverDummy(FlexAllocProblemContext context) {
        super(context, Solver.DUMMY);
        this.profileLength = context.getEnergyProfileToMinimizeWithFlex().length();
    }

    @Override
    protected MpProblem getProblem() {
        return null;
    }

    @Override
    public AllocResults getSolution() {
        return this.results;
    }

    @Override
    protected void processResults(final Optional<MpResult> result) {
        final ListMultimap<FlexibilityProvider, Boolean> allocResults = ArrayListMultimap
                .create();

        for (final FlexibilityProvider p : getProviders()) {
            for (int i = 0; i < profileLength; i++) {
                allocResults.put(p, false);
            }
        }
        this.results = AllocResults.create(allocResults, -1);
    }
}
