package be.kuleuven.cs.gridflex.experimentation.tosg.adapters;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.solvers.data.AllocResults;
import com.google.common.collect.ListMultimap;

/**
 * Adapts the solution results from the solvers to domain level solution results.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class SolutionResultAdapter {

    private final AllocResults target;

    /**
     * Default constructor
     *
     * @param solution The solution to adapt.
     */
    public SolutionResultAdapter(AllocResults solution) {
        this.target = solution;
    }

    private ListMultimap<FlexibilityProvider, Boolean> getAllocationMaps() {
        return target.getAllocationResults();
    }

    /**
     * @return The results.
     */
    public SolutionResults getResults() {
        return SolutionResults.create(getAllocationMaps(), target.getObjective(),
                target.getObjectiveRelativeToUnconstrainedOptimal(), 4);
    }
}
