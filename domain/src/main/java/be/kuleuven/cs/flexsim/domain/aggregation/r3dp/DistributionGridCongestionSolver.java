package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DistributionGridCongestionSolver extends FlexibilityUtiliser<SolutionResults> {

    private TimeSeries congestion;
    @Nullable
    private SolutionResults results;

    public DistributionGridCongestionSolver(AbstractSolverFactory<SolutionResults> fac,
            TimeSeries c) {
        super(fac);
        congestion = c;
    }

    @Override
    protected Solver<SolutionResults> configureSolver() {
        return getSolverFactory().createSolver(new FlexAllocProblemContext() {
            @Override
            public Iterable<FlexibilityProvider> getProviders() {
                return Collections.unmodifiableSet(getFlexibilityProviders());
            }

            @Override
            public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                return congestion;
            }
        });
    }

    @Override
    protected void performSolveStep(Solver<SolutionResults> s) {
        s.solve();
        this.results = s.getSolution();
        processActivations(results);
    }

    private void processActivations(@Nullable SolutionResults results) {
        for (FlexibilityProvider p : getFlexibilityProviders()) {
            processActivationsFor(p, results.getAllocationMaps().get(p),
                    results.getDiscretisationInNbSlotsPerHour());
        }
    }

    private void processActivationsFor(FlexibilityProvider p, List<Boolean> booleen,
            int discretisationInNbSlotsPerHour) {
        int ind = 0;
        while (ind < booleen.size()) {
            if (booleen.get(ind)) {
                //start activation;
                int start = ind;
                int count = 0;
                while (ind < booleen.size() && booleen.get(ind)) {
                    count++;
                    ind++;
                }
                p.registerActivation(FlexActivation
                        .create(start / (double) discretisationInNbSlotsPerHour,
                                count / (double) discretisationInNbSlotsPerHour,
                                count * p.getFlexibilityActivationRate().getUp()
                                        / (double) discretisationInNbSlotsPerHour));
            }
            ind++;
        }
    }

    @Override
    protected SolutionResults getResult() {
        return results;
    }
}
