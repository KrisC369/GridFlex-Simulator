package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.domain.util.CongestionProfile;
import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.MpSolver;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DSOOptimalSolver extends OptimalSolver {
    public DSOOptimalSolver(CongestionProfile c, int i) {

    }

    @Override
    protected void processResults(MpResult result) {

    }

    @Override
    public MpSolver getSolver() {
        return null;
    }

    @Override
    public MpProblem getProblem() {
        return null;
    }
}
