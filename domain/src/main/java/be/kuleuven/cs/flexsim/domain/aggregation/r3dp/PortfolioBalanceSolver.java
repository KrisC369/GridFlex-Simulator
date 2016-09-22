package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.NetRegulatedVolumeProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.PositiveImbalancePriceProfile;

/**
 * Represents a portfolio balancing entity that solves intraday imbalances because of prediction
 * error in portfolios.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolver extends DistributionGridCongestionSolver {

    private static final double FIXED_PRICE = 35.4;
    private final NetRegulatedVolumeProfile nrv;
    private final PositiveImbalancePriceProfile pip;

    /**
     * Default constructor
     *
     * @param fac   The solver factory to draw solver from.
     * @param c     The initial imbalance profile to transform to imbalances.
     * @param specs The turbine specifications of the problem site.
     * @param gen   The wind speed error generator to use to realise different profiles.
     */
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            CableCurrentProfile c, NetRegulatedVolumeProfile nrv, PositiveImbalancePriceProfile pip,
            TurbineSpecification specs, WindErrorGenerator gen) {
        super(fac, convertProfile(c, specs, gen));
        this.nrv = nrv;
        this.pip = pip;
    }

    static CongestionProfile convertProfile(CableCurrentProfile c, TurbineSpecification specs,
            WindErrorGenerator randomGen) {
        return new TurbineProfileConvertor(c, specs, randomGen).convertProfileToImbalanceVolumes();
    }

    @Override
    protected double calculatePaymentFor(FlexActivation activation) {
        return activation.getEnergyVolume() * FIXED_PRICE;
    }
}
