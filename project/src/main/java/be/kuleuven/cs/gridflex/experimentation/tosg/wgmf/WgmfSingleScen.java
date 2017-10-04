package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import com.google.common.collect.ListMultimap;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Game runner for wgmf games in single scenario instances..
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class WgmfSingleScen
        extends WgmfMultiJobGameRunnerVariableFlexParams {
    private static final Logger logger = getLogger(
            WgmfSingleScen.class);

    /**
     * Public constructor from params object and exec strategy.
     *
     * @param expP The experiment parameters.
     */
    WgmfSingleScen(ExperimentParams expP) {
        super(expP);
    }

    /**
     * Main method. Start execution at this point.
     *
     * @param args The arguments passed.
     */
    public static void main(String[] args) {
        startExecution(args,
                WgmfSingleScen::new);
    }

    @Override
    protected void configureExperiments(WgmfGameParams params, int agents,
            List<GenericTask<OptaExperimentResults>> executables,
            ListMultimap<HourlyFlexConstraints, GenericTask<OptaExperimentResults>> experiments) {
        long seed = BASE_SEED;
        HourlyFlexConstraints activationConstraints = params.getActivationConstraints();
        for (int rep = 0; rep < getnReps(); rep++) {
            GenericTask<OptaExperimentResults> optaJppfTaskDSO = new OptaJppfTaskDSO(
                    params, seed + rep, agents, activationConstraints);
            executables.add(optaJppfTaskDSO);
            experiments.put(activationConstraints, optaJppfTaskDSO);
        }
    }

    @Override
    protected ExecutionStrategy getStrategy() {
        return ExecutionStrategy.LOCAL_SERIAL;
    }
}