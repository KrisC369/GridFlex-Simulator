package be.kuleuven.cs.flexsim.solver.optimal.dummy;

import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.MpSolver;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class SolverDummy implements MpSolver {
    @Override
    public void add(MpProblem problem) {

    }

    @Override
    public MpResult solve() {
        return new MpResult() {
            @Override
            public Number getObjective() {
                return 0;
            }

            @Override
            public boolean getBoolean(Object var) {
                return false;
            }

            @Override
            public Number get(Object var) {
                return 0;
            }

            @Override
            public void put(Object var, Number value) {
            }

            @Override
            public Boolean containsVar(Object var) {
                return true;
            }
        };
    }

    @Override
    public void setTimeout(int value) {

    }

    @Override
    public void setVerbose(int value) {

    }
}
