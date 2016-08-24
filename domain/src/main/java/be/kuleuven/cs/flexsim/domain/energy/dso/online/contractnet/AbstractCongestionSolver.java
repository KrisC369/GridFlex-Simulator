package be.kuleuven.cs.flexsim.domain.energy.dso.online.contractnet;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import be.kuleuven.cs.flexsim.protocol.contractnet.ContractNetInitiator;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.util.FastMath;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Entity that solves congestion on local distribution grids by contracting DSM
 * partners and other solutions.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class AbstractCongestionSolver implements SimulationComponent {
    protected static final int DSM_ALLOCATION_DURATION = 4 * 2;
    private final int relativeMaxValuePercent;
    private final TimeSeries congestion;
    private final List<DSMPartner> dsms;
    private int tick;
    private final int forecastHorizon;
    private BigDecimal remediedCongestionCount;
    private final CongestionProfile afterDSMprofile;
    private DoubleList horizon;

    /**
     * Default constructor.
     *
     * @param profile          The congestion profile to solve.
     * @param forecastHorizon  The forecast horizon.
     * @param maxRelativeValue The maximum value between (1-100) as a percent of the maximum
     *                         reference energy amount that should be ignored by the
     *                         mechanism. The maximum reference energy amount is defined as
     *                         the peak power rate in the profile times the forecast horizon.
     */
    public AbstractCongestionSolver(final CongestionProfile profile,
            final int forecastHorizon, final int maxRelativeValue) {
        this.congestion = profile;
        this.dsms = Lists.newArrayList();
        this.tick = 0;
        this.forecastHorizon = forecastHorizon;
        this.remediedCongestionCount = BigDecimal.ZERO;
        this.afterDSMprofile = CongestionProfile.createFromTimeSeries(profile);
        this.horizon = getNewEmptyDouble();
        this.relativeMaxValuePercent = maxRelativeValue;
    }

    /**
     * Register this dsm partner to this solver instance.
     *
     * @param dsm the partner to add.
     */
    public void registerDSMPartner(final DSMPartner dsm) {
        dsms.add(dsm);
        getSolverInstance().registerResponder(dsm.getDSMAPI());
    }

    @Override
    public void initialize(final SimulationContext context) {
    }

    @Override
    public void afterTick(final int t) {
        getWorkResults();
        incrementTick();
        updateHorizon();
    }

    @Override
    public void tick(final int t) {
        doTick();
    }

    private void getWorkResults() {
        double toCorrect = afterDSMprofile.value(getTick());
        for (final DSMPartner d : getDsms()) {
            final double dsmv = d.getCurtailment(getTick()) / 4.0;
            if (dsmv < 0) {
                throw new IllegalStateException(
                        "curtail power cannot be negative");
            }
            if (this.remediedCongestionCount.signum() < 0) {
                throw new IllegalStateException(
                        "Remedied congestion value cannot become negative");
            }
            if (toCorrect > 0) {
                if (dsmv >= toCorrect) {
                    this.remediedCongestionCount = this.remediedCongestionCount
                            .add(BigDecimal.valueOf(toCorrect));
                    toCorrect = 0;
                } else {
                    this.remediedCongestionCount = this.remediedCongestionCount
                            .add(BigDecimal.valueOf(dsmv));
                    toCorrect -= dsmv;
                }
            }
            this.afterDSMprofile.changeValue(getTick(),
                    afterDSMprofile.value(getTick()) - dsmv);
        }
    }

    private void updateHorizon() {
        this.horizon = getNewEmptyDouble();
        for (int i = 0; i < FastMath.min(getForecastHorizon(),
                afterDSMprofile.length() - getTick() - 1); i++) {
            final double toCorrect = afterDSMprofile.value(getTick() + i);
            double correction = 0;
            for (final DSMPartner d : getDsms()) {
                correction += d.getCurtailment(getTick() + i) / 4.0;
            }
            horizon.set(i, FastMath.max(0, toCorrect - correction));
        }
    }

    DoubleList getHorizon() {
        return new DoubleArrayList(this.horizon);
    }

    private void doTick() {
        getSolverInstance().sollicitWork();
    }

    /**
     * @return the tick
     */
    protected final int getTick() {
        return tick;
    }

    private void incrementTick() {
        this.tick = tick + 1;
    }

    /**
     * @return the congestion
     */
    public TimeSeries getCongestion() {
        return this.congestion;
    }

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return Collections.emptyList();
    }

    /**
     * @return the dsms
     */
    public List<DSMPartner> getDsms() {
        return Lists.newArrayList(this.dsms);
    }

    /**
     * @return the solverInstance
     */
    protected abstract ContractNetInitiator<DSMProposal> getSolverInstance();

    /**
     * @return the forecastHorizon
     */
    public int getForecastHorizon() {
        return this.forecastHorizon;
    }

    /**
     * @return Returns the total remedied congestion so far.
     */
    public double getTotalRemediedCongestion() {
        return this.remediedCongestionCount.doubleValue();
    }

    /**
     * @return Returns the remaining congestion profile after application of
     * dsm.
     */
    public CongestionProfile getProfileAfterDSM() {
        return CongestionProfile.createFromTimeSeries(afterDSMprofile);
    }

    protected CongestionProfile getModifiableProfileAfterDSM() {
        return this.afterDSMprofile;
    }

    /**
     * Get a description of the work that is required in the form of a DSM
     * proposal. Can also provide an empty optional value if no work is
     * required.
     *
     * @return an Optional containing a dsm proposal for work in this time
     * period or an empty value.
     */
    protected Optional<DSMProposal> getWorkProposal() {
        double sum = 0;
        final Min m = new Min();
        m.setData(new double[] { DSM_ALLOCATION_DURATION,
                getCongestion().length() - getTick() - 1 });
        for (int i = 0; i < m.evaluate(); i++) {
            sum += getHorizon().getDouble(i);
        }
        if ((sum / (getCongestion().max() * 8.0)
                * 100) < relativeMaxValuePercent) {
            return Optional.empty();
        }

        return Optional.of(DSMProposal.create(
                "CNP for activation for tick: " + getTick(), getCongestion().value(getTick()), 0,
                getTick(), getTick() + DSM_ALLOCATION_DURATION));
    }

    private static DoubleList getNewEmptyDouble() {
        final DoubleList d = new DoubleArrayList(DSM_ALLOCATION_DURATION);
        for (int i = 0; i < DSM_ALLOCATION_DURATION; i++) {
            d.add(0);
        }
        return d;
    }
}
