package be.kuleuven.cs.gridflex.domain.aggregation.r3dp;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;

import java.util.List;

import static java.lang.StrictMath.min;

/**
 * Represents a distribution grid management entity that solves current congestion problems in
 * distribution grid infrastructure.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DistributionGridCongestionSolver extends AbstractFlexAllocationSolver {
    private static final double FIXED_PRICE = 35.4;
    protected static final double TO_KILO = 10E3d;
    private final CongestionProfile congestion;
    private final double flex_remuneration;

    /**
     * Default constructor
     *
     * @param fac The factory that provides the solvers.
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
            double budgetValue = (flex_remuneration / TO_KILO) * resolved;
            double part = singleStepActVolume / singleStepTotalVolume;
            sum += part * budgetValue;
        }
        return sum;
    }

    protected CongestionProfile getBaseProfile() {
        return this.congestion;
    }

    /**
     * @return The congestion volume profile to resolve.
     */
    @Override
    public TimeSeries getCongestionVolumeToResolve() {
        return this.congestion;
    }
}
