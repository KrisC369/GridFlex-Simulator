package be.kuleuven.cs.gridflex.experimentation.runners.local;

import be.kuleuven.cs.gridflex.experimentation.runners.ExperimentRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Thread.sleep;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class MultiThreadedExperimentRunner implements ExperimentRunner {

    private static final Logger logger = LoggerFactory
            .getLogger(MultiThreadedExperimentRunner.class);
    private static final int SLEEPTIME = 100;
    private final ExecutorService executor;
    private Optional<List<Future<Object>>> futures;

    /**
     * Run the experiment multi-threaded with a specified number of threads.
     *
     * @param threads The number of threads.
     */
    MultiThreadedExperimentRunner(final int threads) {
        this(Executors.newFixedThreadPool(threads));
    }

    protected MultiThreadedExperimentRunner(final ExecutorService exec) {
        this.executor = exec;
        this.futures = Optional.empty();
    }

    /**
     * Run the experiments in different threads, varying a parameter between 0
     * and 1 with a step size defined at construction.
     *
     * @param experiments The experiment collection to take tasks from.
     */
    @Override
    public void runExperiments(Collection<? extends Callable<Object>> experiments) {
        try {
            this.futures = Optional.of(executor.invokeAll(experiments));
        } catch (final InterruptedException e1) {
            LoggerFactory.getLogger(MultiThreadedExperimentRunner.class)
                    .warn("Interrupt called during invocation", e1);
        }
        executor.shutdown();
    }

    /**
     * Returns whether this runner has actual threads running.
     *
     * @return true if internal thread count > 0.
     */
    @Override
    public synchronized boolean isRunning() {
        return !this.executor.isTerminated();
    }

    @Override
    public List<Future<Object>> waitAndGetResults() {
        if (!futures.isPresent()) {
            throw new IllegalStateException("Experiments have not run yet.");
        }
        while (isRunning()) {
            try {
                sleep(SLEEPTIME);
            } catch (InterruptedException e) {
                logInterrupt(e);
            }
        }
        return futures.get();
    }

    private void logInterrupt(InterruptedException e) {
        if (logger.isWarnEnabled()) {
            logger.warn("Interrupt caught.", e);
        }
    }
}
