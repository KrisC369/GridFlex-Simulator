package be.kuleuven.cs.gridflex.domain.aggregation.r3dp;

import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.MultiHorizonErrorGenerator;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.MultiHorizonNormalErrorGenerator;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.SolverInputData;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.transformation
        .PowerForecastBasedConverter;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.data.transformation.TurbineProfileConverter;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static be.kuleuven.cs.gridflex.domain.aggregation.r3dp.PortfolioBalanceSolver
        .ProfileConversionStrategy.WINDSPEED_ERROR_BASED;
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
    private static final Logger logger = LoggerFactory.getLogger(PortfolioBalanceSolver.class);

    /**
     * Default constructor
     *
     * @param fac       The solvers factory to draw solvers from.
     * @param inputData the input data.
     */
    @Deprecated
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            SolverInputData inputData) {
        this(fac, inputData, WINDSPEED_ERROR_BASED);
        logger.warn("Windspeed error based solver used. Power based calculations are better.");
    }

    /**
     * Default constructor
     *
     * @param fac       The solvers factory to draw solvers from.
     * @param inputData the input data.
     * @param strategy  the strategy for converting the input data to congestion profiles.
     */
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            SolverInputData inputData, ProfileConversionStrategy strategy) {
        super(fac);
        this.budget = BudgetTracker
                .createDayAheadSellingPrice(inputData.getPositiveImbalancePriceProfile(),
                        inputData.getDayAheadPriceProfile());
        this.congestion = applyBudgetConstraintFilters(strategy.applyConversion(inputData),
                inputData);
        if (logger.isDebugEnabled()) {
            logData(congestion, inputData);
        }
    }

    private void logData(CongestionProfile congestion, SolverInputData inputData) {
        final double TO_POWER = 1.73 * 15.6;
        final double CONVERSION = 1.5d;
        final double SLOTS_PER_HOUR = 4;
        double toResolve = congestion.sum();
        double total = inputData.getCableCurrentProfile()
                .transform(p -> (p / CONVERSION) * TO_POWER)
                .transform(p -> p * CONVERSION / SLOTS_PER_HOUR).sum();

        logger.debug("Totals to solve: {},\nPercentage of total: {}", toResolve, toResolve / total);
    }

    private CongestionProfile applyBudgetConstraintFilters(CongestionProfile profile,
            SolverInputData input) {
        //Only neg NRV should be corrected.
        CongestionProfile negOnly = profile
                .transformFromIndex(i -> input.getNetRegulatedVolumeProfile().value(i) < 0 ?
                        profile.value(i) : 0);
        //Only positive budgets are useful.
        return negOnly
                .transformFromIndex(i -> budget.getBudgetForPeriod(i) < 0 ? 0 : negOnly.value(i));
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

    /**
     * Conversion strategy implementation.
     */
    public enum ProfileConversionStrategy {
        /**
         * Windspeed forecast based error conversion makes use of the turbine convertor to
         * deaggregate wind production data to single turbine level, inferring the corresponding
         * wind speeds and then applying the wind forecast error data on the wind speeds before
         * converting the data back to aggregated energy volumes.
         */
        WINDSPEED_ERROR_BASED {
            @Override
            public CongestionProfile applyConversion(SolverInputData input) {
                logger.debug("Profile conversion chosen = Windspeed error based.");
                MultiHorizonNormalErrorGenerator gen = new MultiHorizonNormalErrorGenerator(
                        input.getSeed(),
                        input.getWindSpeedForecastMultiHorizonErrorDistribution());
                return new TurbineProfileConverter(input.getCableCurrentProfile(),
                        input.getTurbineSpecifications(), gen)
                        .convertProfileToPositiveOnlyImbalanceVolumes();
            }
        },
        /**
         * Power forecast error based conversion applies error data directly on the nominal power
         * production rates.
         */
        POWER_ERROR_BASED {
            @Override
            public CongestionProfile applyConversion(SolverInputData input) {
                logger.debug("Profile conversion chosen = Power error based.");
                MultiHorizonErrorGenerator gen = input.getForecastErrorDistributionType()
                        .createErrorGenerator(input.getSeed(),
                                input.getPowerForecastMultiHorizonErrorDistribution());
                return new PowerForecastBasedConverter(input.getCableCurrentProfile(),
                        gen).convertProfileToPositiveOnlyImbalanceVolumes();
            }
        };

        /**
         * Converts the input data to a valid congestion profile.
         *
         * @param input The input data.
         * @return a congestion profile.
         */
        abstract public CongestionProfile applyConversion(SolverInputData input);
    }
}
