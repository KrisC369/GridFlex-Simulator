/**
 * 
 */
package be.kuleuven.cs.flexsim.experimentation.swift;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.annotation.Nullable;

import be.kuleuven.cs.flexsim.domain.energy.dso.AbstractCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.CompetitiveCongestionSolver;
import be.kuleuven.cs.flexsim.domain.energy.dso.DSMPartner;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class SwiftPOCRunnerComp {

    private static final int POWERRATE = 618;
    private static final int SIMDURATION = 4 * 24 * 365;

    /**
     * @param args
     */
    public static void main(String[] args) {
        SwiftPOCRunnerComp r = new SwiftPOCRunnerComp();
        r.startExperiment();
        r.displayEfficiency();
    }

    private @Nullable CongestionProfile profile;
    private AbstractCongestionSolver solver;
    private DSMPartner partner1;
    private DSMPartner partner2;
    private Simulator sim;

    public SwiftPOCRunnerComp() {
        try {
            this.profile = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkNotNull(profile);
        this.solver = new CompetitiveCongestionSolver(profile, 8);
        this.partner1 = new DSMPartner(POWERRATE);
        this.partner2 = new DSMPartner(POWERRATE / 2);
        solver.registerDSMPartner(partner1);
        solver.registerDSMPartner(partner2);
        sim = Simulator.createSimulator(SIMDURATION);
        sim.register(solver);
    }

    public void startExperiment() {
        this.sim.start();
    }

    private void displayEfficiency() {
        double eff = solver.getTotalRemediedCongestion()
                / ((40.0 * 2.0 * POWERRATE) + (40.0 * 2.0 * POWERRATE / 2));
        System.out.println("Efficiency is " + eff);
    }
}
