/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.domain.energy.dso.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.DSMPartner;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentInstance {

    private static final int SIMDURATION = 4 * 24 * 365;
    private final AbstractCongestionSolver solver;
    private final List<DSMPartner> partners;
    private final Simulator sim;
    private final boolean allowLessActivations;

    /**
     * Default Constructor
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
     */
    public ExperimentInstance(SolverBuilder b, double[] powerRealisation,
            CongestionProfile profile, boolean allow) {
        checkNotNull(profile);
        this.solver = b.getSolver(profile, 8);
        this.partners = Lists.newArrayList();
        allocateAgents(powerRealisation);
        sim = Simulator.createSimulator(SIMDURATION);
        sim.register(solver);
        this.allowLessActivations = allow;
    }

    private void allocateAgents(double[] agents) {
        for (double a : agents) {
            DSMPartner p = new DSMPartner((int) a);
            partners.add(p);
            solver.registerDSMPartner(p);
        }
    }

    /**
     * Start the experiment
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
                / (getTotalPowerRates() * 40.0 * 2.0);
    }

    /**
     * @return the activation rate metric.
     */
    public double getActivationRate() {
        int sum = 0;
        for (DSMPartner p : partners) {
            sum += p.getCurrentActivations();
        }
        return sum / (double) (partners.size()
                * partners.get(0).getMaxActivations());
    }

    private double getTotalPowerRates() {
        int sum = 0;
        for (DSMPartner p : partners) {
            sum += p.getFlexPowerRate();
        }
        return sum;
    }

    /**
     * @return get the summed agent efficiency metric.
     */
    public double getSummedAgentEfficiency() {
        double eff2R = 0;
        for (DSMPartner d : partners) {
            double sum = 0;
            for (int i = 0; i < SIMDURATION; i++) {
                sum += d.getCurtailment(i) / 4;
            }
            if (sum != 0) {
                eff2R += (sum / (d.getCurrentActivations()
                        * d.getFlexPowerRate() * 2));
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

    private void verify() {
        if (!allowLessActivations) {
            for (DSMPartner p : partners) {
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

}
