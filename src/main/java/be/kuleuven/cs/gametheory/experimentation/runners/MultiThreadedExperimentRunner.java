package be.kuleuven.cs.gametheory.experimentation.runners;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class MultiThreadedExperimentRunner {

    private final int threads;
    private int threadCount;
    private RunnableExperiment experiment;
    private static final double DEF_STEPSIZE = 0.10;
    private final double stepSize;
    private static final int SLEEP_TIME = 500;
    private final double factor;

    /**
     * Run the experiment multithreaded with a specified number of threads.
     *
     * @param exp
     *            The experiment.
     * @param threads
     *            The number of threads.
     */
    public MultiThreadedExperimentRunner(RunnableExperiment exp, int threads) {
        this(exp, threads, DEF_STEPSIZE);
    }

    /**
     * Run the experiment multithreaded with a specified number of threads.
     *
     * @param exp
     *            The experiment.
     * @param threads
     *            The number of threads.
     * @param stepsize
     *            The step size to use for the varparam.
     */
    public MultiThreadedExperimentRunner(RunnableExperiment exp, int threads,
            double stepsize) {
        this.threadCount = 0;
        this.threads = threads;
        this.experiment = exp;
        this.stepSize = stepsize;
        factor = 1.0 / stepsize;
    }

    /**
     * Run the experiment with all but one processor occupied.
     *
     * @param exp
     *            The experiment.
     */
    public MultiThreadedExperimentRunner(RunnableExperiment exp) {
        this(exp, Runtime.getRuntime().availableProcessors() - 1);
    }

    /**
     * Run the experiments in different threads, varying a parameter between 0
     * and 1 with a step size defined at construction.
     */
    public void runExperiments() {
        for (int d = 0; d <= 1 * factor; d += stepSize * factor) {
            while (threadCount >= threads) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            final double dd = d / factor;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    experiment.doExperimentRun(dd);
                    decreaseThreadCount();
                }
            }).start();
            increaseThreadCount();
        }
    }

    private synchronized void increaseThreadCount() {
        this.threadCount++;
    }

    private synchronized void decreaseThreadCount() {
        this.threadCount--;
    }

    /**
     * Returns whether this runner has actual threads running.
     *
     * @return true if internal thread count > 0.
     */
    public boolean hasThreadsRunning() {
        return this.threadCount > 0;
    }

}
