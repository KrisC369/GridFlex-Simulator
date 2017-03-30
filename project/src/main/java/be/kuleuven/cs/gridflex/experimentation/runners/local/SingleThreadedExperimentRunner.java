package be.kuleuven.cs.gridflex.experimentation.runners.local;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * A single threaded implementation for the runner interface.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class SingleThreadedExperimentRunner
        extends MultiThreadedExperimentRunner {

    /**
     * Run the experiment single-threaded while blocking.
     */
    public SingleThreadedExperimentRunner() {
        super(MoreExecutors.sameThreadExecutor());
    }
}
