/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.annotation.Nullable;

import be.kuleuven.cs.flexsim.domain.energy.dso.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CompetitiveCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CooperativeCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.DSMPartner;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class SwiftPOCRunner {

    private static final int POWERRATE = 618;
    private static final int SIMDURATION = 4 * 24 * 365;
    private static final int TIMEHORIZON = 8;
    private boolean comp = true;

    /**
     * @param args
     */
    public static void main(String[] args) {
        SwiftPOCRunner r = new SwiftPOCRunner();
        r.startExperiment();
        r.displayEfficiency();
    }

    private @Nullable CongestionProfile profile;
    private AbstractCongestionSolver solver;
    private DSMPartner partner1;
    private DSMPartner partner2;
    private Simulator sim;

    /**
     * Default constructor.
     */
    public SwiftPOCRunner() {
        try {
            this.profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (comp) {
            this.solver = new CompetitiveCongestionSolver(checkNotNull(profile),
                    TIMEHORIZON);
        } else {
            this.solver = new CooperativeCongestionSolver(checkNotNull(profile),
                    TIMEHORIZON);
        }
        this.partner1 = new DSMPartner(POWERRATE);
        this.partner2 = new DSMPartner(POWERRATE / 2);
        solver.registerDSMPartner(partner1);
        solver.registerDSMPartner(partner2);
        sim = Simulator.createSimulator(SIMDURATION);
        sim.register(solver);
    }

    /**
     * Start this proof of concept experiment.
     */
    public void startExperiment() {
        this.sim.start();
    }

    private void displayEfficiency() {
        double eff = solver.getTotalRemediedCongestion()
                / ((40.0 * 2.0 * POWERRATE) + (40.0 * 2.0 * POWERRATE / 2));
        System.out.println("Efficiency is " + eff);
    }
}
