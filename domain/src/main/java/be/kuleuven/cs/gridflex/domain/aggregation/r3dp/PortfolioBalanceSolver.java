package be.kuleuven.cs.gridflex.domain.aggregation.r3dp;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.SolverInputData;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;

import java.util.List;

import static org.apache.commons.math3.util.FastMath.min;

/**
 * Represents a portfolio balancing entity that solves intraday imbalances because of prediction
 * error in portfolios.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolver extends AbstractFlexAllocationSolver {

    private final BudgetTracker budget;
    private final CongestionProfile congestion;

    /**
     * Default constructor
     *
     * @param fac       The solvers factory to draw solvers from.
     * @param inputData the input data.
     */
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            SolverInputData inputData) {
        super(fac);
        this.budget = BudgetTracker
                .createDayAheadSellingPrice(inputData.getPositiveImbalancePriceProfile(),
                        inputData.getDayAheadPriceProfile());
        this.congestion = applyWindForecastErrorAndBudgetConstraints(inputData);
    }

    private CongestionProfile applyWindForecastErrorAndBudgetConstraints(SolverInputData input) {
        MultiHorizonErrorGenerator gen = new MultiHorizonErrorGenerator(input.getSeed(),
                input.getForecastHorizonErrorDistribution());
        CongestionProfile profile = new TurbineProfileConvertor(input.getCableCurrentProfile(),
                input.getTurbineSpecifications(), gen)
                .convertProfileTPositiveOnlyoImbalanceVolumes();
        //Only neg NRV should be corrected.
        CongestionProfile negOnly = profile
                .transformFromIndex(i -> input.getNetRegulatedVolumeProfile().value(i) < 0 ?
                        profile.value(i) :
                        0);
        //Only positive budgets are useful.
        return negOnly
                .transformFromIndex(i -> budget.getBudgetForPeriod(i) < 0 ? 0 : profile.value(i));
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
            double resolved = min(getCongestionVolumeToResolve().value(idx + i),
                    singleStepTotalVolume);
            double budgetValue = (budget.getBudgetForPeriod(idx + i) / TO_KILO) * resolved;
            double part = singleStepActVolume / singleStepTotalVolume;
            sum += part * budgetValue;
        }
        return sum;
    }

    @Override
    public TimeSeries getCongestionVolumeToResolve() {
        return this.congestion;
    }
}
