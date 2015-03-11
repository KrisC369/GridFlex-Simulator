package be.kuleuven.cs.flexsim.experimentation.runners.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.LoggerFactory;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentAtom;
import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;

/**
 * An example class running some experiments.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class MultiThreadedExperimentRunner implements ExperimentRunner {

    private final ExecutorService executor;

    /**
     * Run the experiment multi-threaded with a specified number of threads.
     *
     * @param threads
     *            The number of threads.
     */
    public MultiThreadedExperimentRunner(int threads) {
        this(Executors.newFixedThreadPool(threads));
    }

    protected MultiThreadedExperimentRunner(ExecutorService exec) {
        this.executor = exec;
    }

    /**
     * Run the experiments in different threads, varying a parameter between 0
     * and 1 with a step size defined at construction.
     *
     * @param experiments
     *            The experiment collection to take tasks from.
     */
    @Override
    public void runExperiments(Collection<? extends ExperimentAtom> experiments) {
        List<Callable<Object>> todo = new ArrayList<>(experiments.size());
        for (ExperimentAtom e : experiments) {
            todo.add(Executors.callable(e));
        }
        try {
            executor.invokeAll(todo);
        } catch (InterruptedException e1) {
            LoggerFactory.getLogger(MultiThreadedExperimentRunner.class).warn(
                    "Interrupt called during invocation", e1);
        } catch (final Exception e1) {
            LoggerFactory.getLogger(MultiThreadedExperimentRunner.class).warn(
                    "Interrupt called during invocation", e1);
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
}
