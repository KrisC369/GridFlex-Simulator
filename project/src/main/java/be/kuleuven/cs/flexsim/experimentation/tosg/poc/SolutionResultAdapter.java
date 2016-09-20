package be.kuleuven.cs.flexsim.experimentation.tosg.poc;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.solver.optimal.AllocResults;
import com.google.common.collect.ListMultimap;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class SolutionResultAdapter {

    private AllocResults target;

    public SolutionResultAdapter(AllocResults solution) {
        this.target = solution;
    }

    private ListMultimap<FlexibilityProvider, Boolean> getAllocationMaps() {
        return target.getAllocationResults();
    }

    public SolutionResults getResults() {
        return SolutionResults.create(getAllocationMaps(), 4);
    }
}
