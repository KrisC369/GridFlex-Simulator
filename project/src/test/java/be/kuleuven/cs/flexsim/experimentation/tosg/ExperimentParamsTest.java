package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class ExperimentParamsTest {
    @Test
    public void createAndOrderTest() throws Exception {
        int agents = 3;
        int reps = 6;
        boolean remote = false;
        AbstractOptimalSolver.Solver solver = AbstractOptimalSolver.Solver.DUMMY;
        ExperimentParams experimentParams = ExperimentParams
                .create(agents, reps, solver, remote);
        assertEquals(agents, experimentParams.getNAgents());
        assertEquals(reps, experimentParams.getNRepititions());
        assertEquals(solver, experimentParams.getSolver());
        assertEquals(remote, experimentParams.runRemote());
    }

}