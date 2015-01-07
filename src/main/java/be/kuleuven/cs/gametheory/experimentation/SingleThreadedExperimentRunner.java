package be.kuleuven.cs.gametheory.experimentation;

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
}
