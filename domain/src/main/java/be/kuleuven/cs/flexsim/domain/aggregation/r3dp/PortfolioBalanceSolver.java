package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;

/**
 * Represents a portfolio balancing entity that solves intraday imbalances because of prediction
 * error in portfolios.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolver extends DistributionGridCongestionSolver {

    /**
     * Default constructor
     *
     * @param fac   The solver factory to draw solver from.
     * @param c     The initial imbalance profile to transform to imbalances.
     * @param specs The turbine specifications of the problem site.
     * @param gen   The wind speed error generator to use to realise different profiles.
     */
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            CableCurrentProfile c, TurbineSpecification specs, WindErrorGenerator gen) {
        super(fac, convertProfile(c, specs, gen));
    }

    static CongestionProfile convertProfile(CableCurrentProfile c, TurbineSpecification specs,
            WindErrorGenerator randomGen) {
        return new TurbineProfileConvertor(c, specs, randomGen).convertProfileToImbalanceVolumes();
    }
}
