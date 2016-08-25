package be.kuleuven.cs.flexsim.experimentation.tosg.poc;

import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.DistributionGridCongestionSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.PortfolioBalanceSolver;
import be.kuleuven.cs.flexsim.domain.aggregation.r3dp.SolutionResults;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexProvider;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.simulation.Simulator;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PoCRunner {

    private static final String FILE = "2kwartOpEnNeer.csv";
    private static final String COLUMN = "verlies aan energie";
    private static final int NAGENTS = 2;
    //    private final TSOOptimalSolver tso;
    //    private final AbstractOptimalSolver dso;
    private FlexProvider p1;
    private FlexProvider p2;
    private final DistributionGridCongestionSolver dso;
    private final PortfolioBalanceSolver tso;

    private final Simulator s;
    private CongestionProfile c;

    public PoCRunner() {
        try {
            c = (CongestionProfile) CongestionProfile
                    .createFromCSV("4kwartOpEnNeer.csv", "verlies aan energie");

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        s = Simulator.createSimulator(1000);
        tso = new PortfolioBalanceSolver(c);
        dso = new DistributionGridCongestionSolver(c);
        tso.registerFlexProvider(p1);
        dso.registerFlexProvider(p2);
        dso.solve();
        tso.solve();
        SolutionResults r1 = dso.getSolution();
        SolutionResults r2 = tso.getSolution();
        System.out.println(r1);
        System.out.println(r2);
    }

    public static class PoCGame {
    }
}
