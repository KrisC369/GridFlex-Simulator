package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.solvers.Solvers;
import org.junit.Test;

import static be.kuleuven.cs.flexsim.solvers.Solvers.TYPE.DUMMY;
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
        Solvers.TYPE solver = DUMMY;
        ExperimentParams experimentParams = ExperimentParams.builder().setNAgents(agents)
                .setNRepititions(reps).setSolver(solver).setRemoteExecutable(remote).build();
        assertEquals(agents, experimentParams.getNAgents());
        assertEquals(reps, experimentParams.getNRepititions());
        assertEquals(solver, experimentParams.getSolver());
        assertEquals(remote, experimentParams.isRemoteExecutable());
    }

}