package be.kuleuven.cs.flexsim.domain.aggregation.r3dp;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.solver.AbstractSolverFactory;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexActivation;
import be.kuleuven.cs.flexsim.domain.energy.generation.wind.TurbineSpecification;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.NetRegulatedVolumeProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.PositiveImbalancePriceProfile;

import java.util.List;

/**
 * Represents a portfolio balancing entity that solves intraday imbalances because of prediction
 * error in portfolios.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PortfolioBalanceSolver extends DistributionGridCongestionSolver {

    private final NetRegulatedVolumeProfile nrv;
    private final PositiveImbalancePriceProfile pip;

    /**
     * Default constructor
     *
     * @param fac   The solver factory to draw solver from.
     * @param c     The initial imbalance profile to transform to imbalances.
     * @param specs The turbine specifications of the problem site.
     * @param gen   The wind speed error generator to use to realise different profiles.
     * @param nrv   The profile containing the system net regulation volumes.
     * @param pip   The positive imbalance price profiles.
     */
    public PortfolioBalanceSolver(AbstractSolverFactory<SolutionResults> fac,
            CableCurrentProfile c, NetRegulatedVolumeProfile nrv, PositiveImbalancePriceProfile pip,
            TurbineSpecification specs, WindErrorGenerator gen) {
        super(fac, convertProfile(c, specs, gen, nrv));
        this.nrv = nrv;
        this.pip = pip;
    }

    public static CongestionProfile convertProfile(CableCurrentProfile c,
            TurbineSpecification specs, WindErrorGenerator randomGen,
            NetRegulatedVolumeProfile nrv) {
        CongestionProfile profile = new TurbineProfileConvertor(c, specs, randomGen)
                .convertProfileTPositiveOnlyoImbalanceVolumes();
        //Only neg NRV should apply.
        return profile.transformFromIndex(i -> nrv.value(i) < 0 ? profile.value(i) : 0);
    }

    @Override
    protected double calculatePaymentFor(FlexActivation activation,
            int discretisationInNbSlotsPerHour, List<Integer> acts) {
        int idx = (int) (activation.getStart() * discretisationInNbSlotsPerHour);
        int dur = (int) (activation.getDuration() * discretisationInNbSlotsPerHour);
        double singleStepVolume = activation.getEnergyVolume() / discretisationInNbSlotsPerHour;
        double sum = 0;
        for (int i = 0; i < dur; i++) {
            if (nrv.value(idx + i) < 0) {
                sum += (pip.value(idx + i) / TO_KILO) * singleStepVolume / (double) acts
                        .get(idx + i);
            }
        }
        //TODO resolve imbalance in other direction.

        return sum;
    }
}
