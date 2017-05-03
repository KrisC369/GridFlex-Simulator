package be.kuleuven.cs.gridflex.domain.aggregation.r3dp;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.gridflex.domain.util.Payment;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class AbstractFlexAllocationSolver extends FlexibilityUtiliser<SolutionResults> {
    @Nullable
    private SolutionResults results;

    public AbstractFlexAllocationSolver(AbstractSolverFactory<SolutionResults> solver) {
        super(solver);
    }

    @Override
    protected void performSolveStep() {
        Solver<SolutionResults> solver = getSolverFactory()
                .createSolver(new FlexAllocProblemContext() {
                    @Override
                    public Collection<FlexibilityProvider> getProviders() {
                        return Collections.unmodifiableSet(getFlexibilityProviders());
                    }

                    @Override
                    public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                        return getCongestionVolumeToResolve();
                    }
                });
        this.results = solver.solve();
        processActivations(results);
    }

    private void processActivations(@Nullable final SolutionResults results) {
        if (!getFlexibilityProviders().isEmpty() && !results.equals(SolutionResults.INFEASIBLE)) {
            List<Integer> acts = getTotalActivationsProfile(results.getAllocationMaps());
            List<Double> volumes = getTotalActivatedVolumesProfile(results.getAllocationMaps());

            for (final FlexibilityProvider p : getFlexibilityProviders()) {
                processActivationsFor(p, results.getAllocationMaps().get(p),
                        results.getDiscretisationInNbSlotsPerHour(), acts, volumes);
            }
        }
    }

    @VisibleForTesting
    List<Integer> getTotalActivationsProfile(
            ListMultimap<FlexibilityProvider, Boolean> activationProf) {
        checkArgument(!activationProf.isEmpty(), "ActivationProf provided should not be empty.");
        List<Integer> sizes = Lists.newArrayList();
        activationProf.keySet().forEach(p -> sizes.add(activationProf.get(p).size()));
        int min = sizes.get(0);
        if (!sizes.isEmpty()) {
            min = Collections.min(sizes);
        }
        IntList toRet = new IntArrayList(min);
        for (int j = 0;
             j < min; j++) {
            final int jj = j;
            toRet.add(
                    activationProf.keySet().stream()
                            .mapToInt(fp -> activationProf.get(fp).get(jj) ? 1 : 0).sum());
        }
        return toRet;
    }

    @VisibleForTesting
    List<Double> getTotalActivatedVolumesProfile(
            ListMultimap<FlexibilityProvider, Boolean> activationProf) {
        List<Integer> sizes = Lists.newArrayList();
        activationProf.keySet().forEach(p -> sizes.add(activationProf.get(p).size()));
        int min = Collections.min(sizes);
        DoubleList toRet = new DoubleArrayList(min);
        for (int j = 0;
             j < min; j++) {
            final int jj = j;
            toRet.add(
                    activationProf.keySet().stream()
                            .mapToDouble(fp -> activationProf.get(fp).get(jj) ?
                                    fp.getFlexibilityActivationRate().getUp() :
                                    0d).sum());
        }
        return toRet;
    }

    private void processActivationsFor(final FlexibilityProvider p,
            final List<Boolean> allocMap, final int discretisationInNbSlotsPerHour,
            List<Integer> totalActs, List<Double> totalVolumes) {
        int ind = 0;
        while (ind < allocMap.size()) {
            if (allocMap.get(ind)) {
                //start activation.
                final int start = ind;
                int count = 0;
                while (ind < allocMap.size() && allocMap.get(ind)) {
                    count++;
                    ind++;
                }
                FlexActivation activation = FlexActivation
                        .create(start / (double) discretisationInNbSlotsPerHour,
                                count / (double) discretisationInNbSlotsPerHour,
                                count * p.getFlexibilityActivationRate().getUp()
                                        / (double) discretisationInNbSlotsPerHour);
                p.registerActivation(activation, Payment.create(
                        calculatePaymentFor(activation, discretisationInNbSlotsPerHour, totalActs,
                                totalVolumes)));
            }
            ind++;
        }
    }

    protected abstract double calculatePaymentFor(FlexActivation activation,
            int discretisationInNbSlotsPerHour, List<Integer> acts, List<Double> totalVolumes);

    @Override
    protected SolutionResults getResult() {
        return results;
    }

    public abstract TimeSeries getCongestionVolumeToResolve();
}
