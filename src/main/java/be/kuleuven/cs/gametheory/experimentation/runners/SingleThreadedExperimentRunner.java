package be.kuleuven.cs.gametheory.experimentation.runners;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class SingleThreadedExperimentRunner extends
        MultiThreadedExperimentRunner {

    /**
     * Run the experiment with all but one processor occupied.
     *
     * @param exp
     *            The experiment.
     */
    public SingleThreadedExperimentRunner(RunnableExperiment exp) {
        super(exp, 1);
    }

    /**
     * Run the experiment with all but one processor occupied.
     *
     * @param exp
     *            The experiment.
     * @param stepsize
     *            The step size of the varparam increment.
     */
    public SingleThreadedExperimentRunner(RunnableExperiment exp,
            double stepsize) {
        super(exp, 1, stepsize);
    }
}
