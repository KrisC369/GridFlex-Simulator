package be.kuleuven.cs.gametheory.experimentation;

/**
 * An example class running some experiments.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class MultiThreadedExperimentRunner {

    private final int threads;
    private volatile int threadCount;
    private RunnableExperiment experiment;

    /**
     * Run the experiment multithreaded with a specified number of threads.
     * 
     * @param exp
     *            The experiment.
     * @param threads
     *            The number of threads.
     */
    public MultiThreadedExperimentRunner(RunnableExperiment exp, int threads) {
        this.threadCount = 0;
        this.threads = threads;
        this.experiment = exp;
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
     * Run the experiments in different threads.
     */
    public void runExperiments() {
        for (double d = 1; d < 500; d += 25) {
            while (threadCount >= threads) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            final double dd = d;
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

}
