/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;

import com.google.common.collect.Lists;

import be.kuleuven.cs.flexsim.domain.energy.dso.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.DSMPartner;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class ExperimentInstance {

    // private static final double R3DP_GAMMA_SCALE = 677.926;
    // private static final double R3DP_GAMMA_SHAPE = 1.37012;
    private static final int POWERRATE = 618;
    private static final int SIMDURATION = 4 * 24 * 365;

    private @Nullable CongestionProfile profile;
    private AbstractCongestionSolver solver;
    private List<DSMPartner> partners;
    private Simulator sim;
    private final AbstractRealDistribution gamma;

    public ExperimentInstance(int nAgents, SolverBuilder b,
            AbstractRealDistribution dist, CongestionProfile profile) {
        // try {
        // this.profile = (CongestionProfile) CongestionProfile
        // .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        checkNotNull(profile);
        // gamma = new GammaDistribution(R3DP_GAMMA_SHAPE, R3DP_GAMMA_SCALE);
        gamma = dist;
        this.solver = b.getSolver(profile, 8);
        this.partners = Lists.newArrayList();
        generateAgents(nAgents);
        sim = Simulator.createSimulator(SIMDURATION);
        sim.register(solver);
    }

    public ExperimentInstance(int nAgents, SolverBuilder b,
            double[] powerRealisation, CongestionProfile profile) {
        // try {
        // this.profile = (CongestionProfile) CongestionProfile
        // .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        checkNotNull(profile);
        this.solver = b.getSolver(profile, 8);
        this.partners = Lists.newArrayList();
        gamma = new GammaDistribution(1, 1);
        allocateAgents(powerRealisation);
        sim = Simulator.createSimulator(SIMDURATION);
        sim.register(solver);
    }

    private void generateAgents(int n) {
        for (int i = 0; i < n; i++) {
            DSMPartner p = new DSMPartner((int) gamma.sample());
            partners.add(p);
            solver.registerDSMPartner(p);
        }
    }

    private void allocateAgents(double[] agents) {
        for (double a : agents) {
            DSMPartner p = new DSMPartner((int) a);
            partners.add(p);
            solver.registerDSMPartner(p);
        }
    }

    public void startExperiment() {
        this.sim.start();
        verify();
    }

    private void displayEfficiency() {
        double eff = solver.getTotalRemediedCongestion()
                / ((40.0 * 2.0 * POWERRATE) + (40.0 * 2.0 * POWERRATE / 2));
        System.out.println("Efficiency is " + eff);
    }

    public double getEfficiency() {
        return solver.getTotalRemediedCongestion()
                / (getTotalPowerRates() * 40.0 * 2.0);
    }

    private double getTotalPowerRates() {
        int sum = 0;
        for (DSMPartner p : partners) {
            sum += p.getFlexPowerRate();
        }
        return sum;
    }

    private void verify() {
        for (DSMPartner p : partners) {
            if (p.getCurrentActivations() < DSMPartner.R3DPMAX_ACTIVATIONS) {
                throw new IllegalStateException("not enough activations. only "
                        + p.getCurrentActivations());
            }
        }
        if (getEfficiency() > 1) {
            throw new IllegalStateException("efficiency can't be > 1");
        }
    }

}
