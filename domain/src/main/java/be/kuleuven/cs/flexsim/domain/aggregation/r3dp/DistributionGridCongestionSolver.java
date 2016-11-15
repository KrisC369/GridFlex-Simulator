package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.Solver;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexAllocProblemContext;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.util.Payment;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import static java.lang.StrictMath.min;

/**
 * Represents a distribution grid management entity that solves current congestion problems in
 * distribution grid infrastructure.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DistributionGridCongestionSolver extends FlexibilityUtiliser<SolutionResults> {
    private static final double FIXED_PRICE = 35.4;
    protected static final double TO_KILO = 10E3d;
    private final CongestionProfile congestion;
    private final double flex_remuneration;
    @Nullable
    private SolutionResults results;

    /**
     * Default constructor
     *
     * @param fac The factory that provides the solver.
     * @param c   The congestion profile
     */
    public DistributionGridCongestionSolver(final AbstractSolverFactory<SolutionResults> fac,
            final CongestionProfile c) {
        this(fac, c, FIXED_PRICE);
    }

    public DistributionGridCongestionSolver(final AbstractSolverFactory<SolutionResults> fac,
            final CongestionProfile profile, double flexRemunerationPrice) {
        super(fac);
        congestion = profile;
        this.flex_remuneration = flexRemunerationPrice;
    }

    @Override
    protected void performSolveStep() {
        Solver<SolutionResults> solver = getSolverFactory()
                .createSolver(new FlexAllocProblemContext() {
                    @Override
                    public Iterable<FlexibilityProvider> getProviders() {
                        return Collections.unmodifiableSet(getFlexibilityProviders());
                    }

                    @Override
                    public TimeSeries getEnergyProfileToMinimizeWithFlex() {
                        return congestion;
                    }
                });
        solver.solve();
        this.results = solver.getSolution();
        processActivations(results);
    }

    private void processActivations(@Nullable final SolutionResults results) {
        if (!getFlexibilityProviders().isEmpty()) {
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
            ListMultimap<FlexibilityProvider, Boolean> values) {
        List<Integer> sizes = Lists.newArrayList();
        values.keySet().forEach(p -> sizes.add(values.get(p).size()));
        int min = Collections.min(sizes);
        IntList toRet = new IntArrayList(min);
        for (int j = 0;
             j < min; j++) {
            final int jj = j;
            toRet.add(
                    values.keySet().stream().mapToInt(fp -> values.get(fp).get(jj) ? 1 : 0).sum());
        }
        return toRet;
    }

    @VisibleForTesting
    List<Double> getTotalActivatedVolumesProfile(
            ListMultimap<FlexibilityProvider, Boolean> values) {
        List<Integer> sizes = Lists.newArrayList();
        values.keySet().forEach(p -> sizes.add(values.get(p).size()));
        int min = Collections.min(sizes);
        DoubleList toRet = new DoubleArrayList(min);
        for (int j = 0;
             j < min; j++) {
            final int jj = j;
            toRet.add(
                    values.keySet().stream().mapToDouble(fp -> values.get(fp).get(jj) ?
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

    protected double calculatePaymentFor(FlexActivation activation,
            int discretisationInNbSlotsPerHour, List<Integer> acts, List<Double> totalVolumes) {
        int idx = (int) (activation.getStart() * discretisationInNbSlotsPerHour);
        int dur = (int) (activation.getDuration() * discretisationInNbSlotsPerHour);
        double singleStepActVolume = activation.getEnergyVolume() / discretisationInNbSlotsPerHour;
        double sum = 0;
        for (int i = 0; i < dur; i++) {
            double singleStepTotalVolume =
                    totalVolumes.get(idx + i) / discretisationInNbSlotsPerHour;
            double resolved = min(getBaseProfile().value(idx + i), singleStepTotalVolume);
            double budgetValue = (FIXED_PRICE / TO_KILO) * resolved;
            double part = singleStepActVolume / singleStepTotalVolume;
            sum += part * budgetValue;
        }
        return sum;
    }

    @Override
    protected SolutionResults getResult() {
        return results;
    }

    protected CongestionProfile getBaseProfile() {
        return this.congestion;
    }
}
