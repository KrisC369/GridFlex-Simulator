/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.DSMPartner;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.AbstractTimeSeriesImplementation;
import be.kuleuven.cs.flexsim.simulation.Simulator;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentInstance {

    private static final int FORECAST_HORIZON = 8;
    private static final int SIMDURATION = 4 * 24 * 365;
    private final AbstractCongestionSolver solver;
    private final List<DSMPartner> partners;
    private final Simulator sim;
    private final boolean allowLessActivations;
    private final double producedE;

    /**
     * Default Constructor.
     * 
     * @param b
     *            SolverBuilder instance
     * @param powerRealisation
     *            Power rates for the agents.
     * @param profile
     *            The congestion profile
     * @param allow
     *            Allow less than the max amount of activations? otherwise fail
     *            experiment.
     * @param producedE
     *            The total energy produced on the feeder.
     */
    public ExperimentInstance(final SolverBuilder b, final DoubleList powerRealisation,
            final AbstractTimeSeriesImplementation profile, final boolean allow, final double producedE) {
        checkNotNull(profile);
        this.solver = b.getSolver(profile, FORECAST_HORIZON);
        this.partners = Lists.newArrayList();
        allocateAgents(powerRealisation);
        sim = Simulator.createSimulator(SIMDURATION);
        sim.register(solver);
        this.allowLessActivations = allow;
        this.producedE = producedE;
    }

    private void allocateAgents(final DoubleList agents) {
        for (final double a : agents) {
            final DSMPartner p = new DSMPartner((int) a);
            partners.add(p);
            solver.registerDSMPartner(p);
        }
    }

    /**
     * Start the experiment.
     */
    public void startExperiment() {
        this.sim.start();
        verify();
    }

    /**
     * @return The efficiency metric.
     */
    public double getEfficiency() {
        return solver.getTotalRemediedCongestion()
                / (getTotalPowerRates() * DSMPartner.R3DPMAX_ACTIVATIONS
                        * DSMPartner.ACTIVATION_DURATION);
    }

    /**
     * @return the activation rate metric.
     */
    public double getActivationRate() {
        int sum = 0;
        for (final DSMPartner p : partners) {
            sum += p.getCurrentActivations();
        }
        return sum / (double) (partners.size()
                * partners.get(0).getMaxActivations());
    }

    private double getTotalPowerRates() {
        int sum = 0;
        for (final DSMPartner p : partners) {
            sum += p.getFlexPowerRate();
        }
        return sum;
    }

    /**
     * @return get the summed agent efficiency metric.
     */
    public double getSummedAgentEfficiency() {
        double eff2R = 0;
        for (final DSMPartner d : partners) {
            double sum = 0;
            for (int i = 0; i < SIMDURATION; i++) {
                sum += d.getCurtailment(i) / 4;
            }
            if (sum != 0) {
                eff2R += (sum
                        / (d.getCurrentActivations() * d.getFlexPowerRate()
                                * DSMPartner.ACTIVATION_DURATION));
            }
        }
        return eff2R;
    }

    /**
     * @return the remedied congestion value.
     */
    public double getRemediedCongestion() {
        return solver.getTotalRemediedCongestion();
    }

    /**
     * @return the remedied congestion relative to the total congestion.
     */
    public double getRemediedCongestionFraction() {
        return getRemediedCongestion() / solver.getCongestion().sum();
    }

    /**
     * @return the amount of remaining congestion as a fraction of the total
     *         produced energy.
     */
    public double getRemediedCongestionRelatedToProducedEnergy() {
        return (solver.getCongestion().sum() - getRemediedCongestion())
                / getProducedE();
    }

    private void verify() {
        if (!allowLessActivations) {
            for (final DSMPartner p : partners) {
                if (p.getCurrentActivations() < DSMPartner.R3DPMAX_ACTIVATIONS) {
                    throw new IllegalStateException(
                            "not enough activations. only "
                                    + p.getCurrentActivations());
                }
            }
        }
        if (getEfficiency() > 1) {
            throw new IllegalStateException("efficiency can't be > 1");
        }
    }

    private double getProducedE() {
        return producedE;
    }
}
