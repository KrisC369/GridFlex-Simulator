package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

/**
 * Experiment parameters.
 */
@AutoValue
public abstract class ExperimentParams implements Serializable {

    ExperimentParams() {
    }

    /**
     * @return The number of agents to use.
     */
    public abstract int getNAgents();

    /**
     * @return The number of repititions to perform.
     */
    public abstract int getNRepititions();

    /**
     * @return The solver to use.
     */
    public abstract AbstractOptimalSolver.Solver getSolver();

    /**
     * @return true if remote execution should be enabled.
     */
    public abstract boolean runRemote();

    /**
     * @return An instance of ExperimentParams.
     */
    public static ExperimentParams create(int nAg, int nr, AbstractOptimalSolver.Solver s,
            boolean remote) {
        return new AutoValue_ExperimentParams(nAg, nr, s, remote);
    }
}
