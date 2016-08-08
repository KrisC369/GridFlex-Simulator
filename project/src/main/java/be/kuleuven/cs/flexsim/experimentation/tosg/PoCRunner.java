package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.optimal.DSOOptimalSolver;
import be.kuleuven.cs.flexsim.experimentation.tosg.optimal.OptimalSolver;
import be.kuleuven.cs.flexsim.experimentation.tosg.optimal.TSOOptimalSolver;
import be.kuleuven.cs.flexsim.simulation.Simulator;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PoCRunner {

    private static final String FILE = "2kwartOpEnNeer.csv";
    private static final String COLUMN = "verlies aan energie";
    private static final int NAGENTS = 2;
    private final TSOOptimalSolver tso;
    private final OptimalSolver dso;
    private FlexProvider p1;
    private FlexProvider p2;
    private final Simulator s;
    private CongestionProfile c;

    public PoCRunner() {
        s = Simulator.createSimulator(1000);
        tso = new TSOOptimalSolver(c, 8);
        dso = new DSOOptimalSolver(c, 8);
        tso.registerFlexrovider(p1);
        dso.registerFlexProvider(p2);

        s.register(tso);
        s.register(dso);
    }

    public static class PoCGame {
    }
}
